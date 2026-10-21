package com.example.snakem;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;
import android.os.Handler;
import android.os.Looper;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class SnakeGameView extends View {
    private static final int COLS = 20, ROWS = 28;
    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Random random = new Random();
    private final ArrayDeque<Cell> snake = new ArrayDeque<>();
    private final ArrayList<Cell> apples = new ArrayList<>();
    private int dirX = 1, dirY = 0, nextX = 1, nextY = 0, score = 0;
    private boolean running = true, gameOver = false, started = false;
    private float cell, boardLeft, boardTop, boardWidth, boardHeight;
    private float joyX, joyY, joyBaseX, joyBaseY;
    private boolean draggingJoy = false;
    private final Runnable ticker = new Runnable() {
        @Override public void run() {
            if (running && started && !gameOver) step();
            invalidate();
            handler.postDelayed(this, 135);
        }
    };

    public SnakeGameView(Context context) {
        super(context);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        resetGame();
        handler.post(ticker);
    }

    private static class Cell {
        int x, y;
        Cell(int x, int y) { this.x = x; this.y = y; }
        @Override public boolean equals(Object o) {
            if (!(o instanceof Cell)) return false;
            Cell c = (Cell)o; return x == c.x && y == c.y;
        }
        @Override public int hashCode() { return x * 1009 + y; }
    }

    private void resetGame() {
        snake.clear(); apples.clear();
        int x = COLS / 2, y = ROWS / 2;
        snake.add(new Cell(x, y)); snake.add(new Cell(x - 1, y)); snake.add(new Cell(x - 2, y));
        dirX = nextX = 1; dirY = nextY = 0;
        score = 0; gameOver = false; started = false; running = true;
        for (int i = 0; i < 3; i++) addApple();
        invalidate();
    }

    private void addApple() {
        for (int tries = 0; tries < 500; tries++) {
            Cell c = new Cell(random.nextInt(COLS), random.nextInt(ROWS));
            if (!snake.contains(c) && !apples.contains(c)) { apples.add(c); return; }
        }
    }

    private void step() {
        dirX = nextX; dirY = nextY;
        Cell head = snake.peekFirst();
        Cell nh = new Cell(head.x + dirX, head.y + dirY);
        if (nh.x < 0 || nh.x >= COLS || nh.y < 0 || nh.y >= ROWS) { gameOver = true; return; }
        boolean eating = apples.contains(nh);
        Iterator<Cell> it = snake.iterator();
        int count = 0;
        while (it.hasNext()) {
            Cell c = it.next();
            if (c.equals(nh) && !( !eating && count == snake.size() - 1)) { gameOver = true; return; }
            count++;
        }
        snake.addFirst(nh);
        if (eating) {
            apples.remove(nh); score += 10; addApple();
        } else snake.removeLast();
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float w = getWidth(), h = getHeight();
        canvas.drawColor(0xFF090D14);
        p.setColor(0xFF101925); p.setStyle(Paint.Style.FILL);
        canvas.drawRoundRect(new RectF(14, 14, w - 14, h - 14), 28, 28, p);

        p.setTextAlign(Paint.Align.LEFT); p.setTypeface(android.graphics.Typeface.create("sans-serif", 1));
        p.setColor(0xFFE8F0FA); p.setTextSize(sp(27));
        canvas.drawText("SNAKE M", 28, 53, p);
        p.setColor(0xFF8FA3BA); p.setTextSize(sp(13));
        canvas.drawText("EAT APPLES • GROW LONGER", 29, 76, p);
        p.setTextAlign(Paint.Align.RIGHT); p.setColor(0xFF65E6A6); p.setTextSize(sp(20));
        canvas.drawText("SCORE " + score, w - 28, 52, p);

        float top = 96, bottom = h - Math.max(180, h * .24f);
        boardWidth = Math.min(w - 34, (bottom - top) * COLS / ROWS);
        boardHeight = boardWidth * ROWS / COLS;
        if (boardHeight > bottom - top) { boardHeight = bottom - top; boardWidth = boardHeight * COLS / ROWS; }
        cell = boardWidth / COLS;
        boardLeft = (w - boardWidth) / 2f;
        boardTop = top + Math.max(0, (bottom - top - boardHeight) / 2f);

        p.setColor(0xFF0B121C);
        canvas.drawRoundRect(new RectF(boardLeft - 2, boardTop - 2, boardLeft + boardWidth + 2, boardTop + boardHeight + 2), 14, 14, p);
        p.setStrokeWidth(1); p.setColor(0xFF172332);
        for (int i = 1; i < COLS; i++) canvas.drawLine(boardLeft + i * cell, boardTop, boardLeft + i * cell, boardTop + boardHeight, p);
        for (int i = 1; i < ROWS; i++) canvas.drawLine(boardLeft, boardTop + i * cell, boardLeft + boardWidth, boardTop + i * cell, p);

        for (Cell a : apples) {
            float cx = boardLeft + (a.x + .5f) * cell, cy = boardTop + (a.y + .5f) * cell;
            p.setColor(0xFFFF5577); canvas.drawCircle(cx, cy, cell * .37f, p);
            p.setColor(0xFFFFA2B3); canvas.drawCircle(cx - cell * .10f, cy - cell * .12f, cell * .09f, p);
            p.setColor(0xFF65E6A6); canvas.drawRect(cx, cy - cell * .48f, cx + cell * .09f, cy - cell * .30f, p);
        }
        int i = 0;
        for (Cell c : snake) {
            float pad = cell * .09f;
            p.setColor(i == 0 ? 0xFF9BFFD0 : 0xFF35D98A);
            canvas.drawRoundRect(new RectF(boardLeft + c.x * cell + pad, boardTop + c.y * cell + pad,
                    boardLeft + (c.x + 1) * cell - pad, boardTop + (c.y + 1) * cell - pad),
                    cell * .25f, cell * .25f, p);
            if (i == 0) {
                p.setColor(0xFF09251A);
                float ex = boardLeft + (c.x + .5f + dirX * .18f) * cell;
                float ey = boardTop + (c.y + .5f + dirY * .18f) * cell;
                canvas.drawCircle(ex - dirY * cell * .16f, ey + dirX * cell * .16f, cell * .07f, p);
                canvas.drawCircle(ex + dirY * cell * .16f, ey - dirX * cell * .16f, cell * .07f, p);
            }
            i++;
        }

        float controlsY = h - Math.max(95, h * .13f);
        float baseX = w * .25f, baseY = controlsY;
        p.setColor(0xFF1D2B3A); canvas.drawCircle(baseX, baseY, 52, p);
        p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(2); p.setColor(0xFF3B5267);
        canvas.drawCircle(baseX, baseY, 52, p); p.setStyle(Paint.Style.FILL);
        float kx = draggingJoy ? joyX : baseX, ky = draggingJoy ? joyY : baseY;
        p.setColor(0xFF65E6A6); canvas.drawCircle(kx, ky, 23, p);
        p.setColor(0xFF0B3927); canvas.drawCircle(kx, ky, 8, p);

        float bx = w * .73f, by = controlsY;
        p.setColor(0xFF1D2B3A); canvas.drawRoundRect(new RectF(bx - 58, by - 37, bx + 58, by + 37), 20, 20, p);
        p.setColor(0xFF65E6A6); p.setTextAlign(Paint.Align.CENTER); p.setTextSize(sp(14));
        canvas.drawText(started ? "PAUSE" : "START", bx, by + 5, p);
        p.setColor(0xFF8FA3BA); p.setTextSize(sp(11));
        canvas.drawText("JOYSTICK TO TURN", w * .25f, controlsY + 78, p);

        if (!started || gameOver || !running) {
            p.setColor(0xCC070B11); canvas.drawRoundRect(new RectF(30, boardTop + boardHeight * .34f, w - 30, boardTop + boardHeight * .66f), 22, 22, p);
            p.setTextAlign(Paint.Align.CENTER); p.setColor(0xFFFFFFFF); p.setTextSize(sp(25));
            canvas.drawText(gameOver ? "GAME OVER" : (!running ? "PAUSED" : "READY?"), w / 2, boardTop + boardHeight * .47f, p);
            p.setColor(0xFF9DB1C7); p.setTextSize(sp(14));
            canvas.drawText(gameOver ? "Tap START to play again" : "Tap START and eat the apples", w / 2, boardTop + boardHeight * .53f, p);
        }
    }

    private float sp(float n) { return n * getResources().getDisplayMetrics().scaledDensity; }

    @Override public boolean onTouchEvent(MotionEvent e) {
        float x = e.getX(), y = e.getY(), w = getWidth(), h = getHeight();
        float controlsY = h - Math.max(95, h * .13f);
        float baseX = w * .25f, baseY = controlsY, bx = w * .73f, by = controlsY;
        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if (x > bx - 75 && x < bx + 75 && y > by - 55 && y < by + 55) {
                    if (gameOver) {
                        resetGame();
                        started = true;
                        running = true;
                    } else if (!started) {
                        started = true;
                        running = true;
                    } else {
                        running = !running;
                    }
                    invalidate(); return true;
                }
                if (x < w * .52f && y > h * .55f) {
                    draggingJoy = true; joyBaseX = baseX; joyBaseY = baseY; joyX = x; joyY = y;
                    updateDirection(x - baseX, y - baseY); invalidate(); return true;
                }
                return true;
            case MotionEvent.ACTION_MOVE:
                if (draggingJoy) {
                    float dx = x - joyBaseX, dy = y - joyBaseY, len = (float)Math.hypot(dx, dy);
                    if (len > 42) { dx *= 42 / len; dy *= 42 / len; }
                    joyX = joyBaseX + dx; joyY = joyBaseY + dy;
                    updateDirection(x - joyBaseX, y - joyBaseY); invalidate();
                }
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                draggingJoy = false; invalidate(); return true;
        }
        return true;
    }

    private void updateDirection(float dx, float dy) {
        if (Math.abs(dx) < 12 && Math.abs(dy) < 12) return;
        if (Math.abs(dx) > Math.abs(dy)) {
            int d = dx > 0 ? 1 : -1;
            if (!(d == -dirX && dirY == 0)) { nextX = d; nextY = 0; }
        } else {
            int d = dy > 0 ? 1 : -1;
            if (!(d == -dirY && dirX == 0)) { nextX = 0; nextY = d; }
        }
        if (!started) started = true;
    }

    @Override protected void onDetachedFromWindow() {
        handler.removeCallbacks(ticker);
        super.onDetachedFromWindow();
    }
}
