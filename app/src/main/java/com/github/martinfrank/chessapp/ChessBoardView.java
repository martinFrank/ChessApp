package com.github.martinfrank.chessapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import androidx.annotation.Nullable;
import com.github.martinfrank.games.chessmodel.model.Game;
import com.github.martinfrank.games.chessmodel.model.chess.Color;
import com.github.martinfrank.games.chessmodel.model.chess.Field;
import com.github.martinfrank.games.chessmodel.model.chess.Figure;
import com.github.martinfrank.games.chessmodel.model.chess.Participant;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ChessBoardView extends View {

    private static final String LOG_TAG = "ChessBoardView";

    private int viewSizeInPixels;
    private final Paint blackBack = new Paint();
    private final Paint whiteBack = new Paint();
    private final Paint blackFigure = new Paint();
    private final Paint whiteFigure = new Paint();
    private final Paint hostColor = new Paint();
    private final Paint guestColor = new Paint();
    private final Paint coordinateColor = new Paint();
    private Game game;

    private static final float FIELD_DIVIDER = 9f;
    private static final float STROKE_WIDTH = 9f;

    public ChessBoardView(Context context) {
        super(context);
        init();
    }

    public ChessBoardView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ChessBoardView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public ChessBoardView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        blackBack.setARGB(0xff, 0xBB, 0, 0);
        blackBack.setStyle(Paint.Style.FILL);
        whiteBack.setARGB(0xff, 0xBB, 0xBB, 0xBB);
        whiteBack.setStyle(Paint.Style.FILL);

        blackFigure.setARGB(0xff, 0xff, 0, 0);
        blackFigure.setStyle(Paint.Style.FILL);
        whiteFigure.setARGB(0xff, 0xFF, 0xFF, 0xFF);
        whiteFigure.setStyle(Paint.Style.FILL);

        hostColor.setStyle(Paint.Style.STROKE);
        guestColor.setStyle(Paint.Style.STROKE);

        coordinateColor.setARGB(0xff, 0, 0, 0);
    }


    @Override
    protected void onMeasure(int width, int height) {
        super.onMeasure(width, height);
        //make it square-shaped
        int measuredWidth = getMeasuredWidth();
        int measuredHeight = getMeasuredHeight();
        viewSizeInPixels = Math.min(measuredWidth, measuredHeight);
        setMeasuredDimension(viewSizeInPixels, viewSizeInPixels);
        blackFigure.setTextSize(viewSizeInPixels / FIELD_DIVIDER);
        whiteFigure.setTextSize(viewSizeInPixels / FIELD_DIVIDER);
        coordinateColor.setTextSize(viewSizeInPixels / FIELD_DIVIDER / 3f);
    }

    public void updateBoard(Game game) {
        this.game = game;
        int hr = ColorConverter.red(game.getHostPlayer().color);
        int hg = ColorConverter.green(game.getHostPlayer().color);
        int hb = ColorConverter.blue(game.getHostPlayer().color);
        hostColor.setARGB(0xff, hr, hg, hb);
        hostColor.setStrokeWidth(STROKE_WIDTH);
        if (game.getGuestPlayer() != null) {
            int gr = ColorConverter.red(game.getGuestPlayer().color);
            int gg = ColorConverter.green(game.getGuestPlayer().color);
            int gb = ColorConverter.blue(game.getGuestPlayer().color);
            guestColor.setARGB(0xff, gr, gg, gb);
            guestColor.setStrokeWidth(STROKE_WIDTH);
        }

    }


    public Field getFieldAt(float x, float y) {
        float rasterSize = viewSizeInPixels / FIELD_DIVIDER;
        float padding = rasterSize / 2;
        if (x - padding < 0) {
            return null;
        }
        if (y - padding < 0) {
            return null;
        }
        int xInt = (int) ((x - padding) / rasterSize);
        int yInt = (int) ((y - padding) / rasterSize);
        Log.d(LOG_TAG, "mapping touch: " + x + "/" + y + " to field at: " + xInt + "/" + yInt);
        try {
            String column = Field.mapToColumn(xInt);
            String row = Field.mapToRow(yInt);
            return new Field(row, column);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float squareSize = viewSizeInPixels / FIELD_DIVIDER;
        drawFields(canvas, squareSize);
        drawCoordinates(canvas, squareSize);
    }

    private void drawCoordinates(Canvas canvas, float squareSize) {
        float offset = squareSize / 2f;
        for (int dx = 0; dx < 8; dx++) {
            String column = Field.mapToColumn(dx);
            float x = (dx * squareSize) + offset + coordinateColor.getTextSize();
            float top = coordinateColor.getTextSize() + 2f;
            float bottom = (8 * squareSize) + offset + coordinateColor.getTextSize() + 2f;
            canvas.drawText(column, x, top, coordinateColor);
            canvas.drawText(column, x, bottom, coordinateColor);
        }
        for (int dy = 0; dy < 8; dy++) {
            String row = Field.mapToRow(dy);
            float left = (squareSize / 4f) - 2f;
            float right = (8 * squareSize) + offset + (squareSize / 8f);
            float y = (dy * squareSize) + offset + (squareSize / 4f) + coordinateColor.getTextSize();
            canvas.drawText(row, left, y, coordinateColor);
            canvas.drawText(row, right, y, coordinateColor);
        }
    }

    private void drawFields(Canvas canvas, float squareSize) {
        for (int dy = 0; dy < 8; dy++) {
            for (int dx = 0; dx < 8; dx++) {
                drawField(dx, dy, squareSize, canvas);
            }
        }
        if (hasBoard()) {
            drawSelection(canvas, squareSize);
        }
    }

    private void drawSelection(Canvas canvas, float squareSize) {

        List<Field> hostSelection = getSelection(game.chessGame.getHost());
        List<Field> guestSelection = getSelection(game.chessGame.getGuest());
        List<Field> intersection = intersect(hostSelection, guestSelection);
        for (Field selectionPath : hostSelection) {
            Rect fRect = mapToRect(Field.mapFromColumn(selectionPath.column), Field.mapFromRow(selectionPath.row), squareSize);
            canvas.drawRect(fRect, hostColor);
        }
        for (Field selectionPath : guestSelection) {
            Rect fRect = mapToRect(Field.mapFromColumn(selectionPath.column), Field.mapFromRow(selectionPath.row), squareSize);
            canvas.drawRect(fRect, guestColor);
        }
        hostColor.setPathEffect(new DashPathEffect(new float[]{10, 10}, 0));
        guestColor.setPathEffect(new DashPathEffect(new float[]{10, 10}, 10));
        for (Field selectionPath : intersection) {
            Rect fRect = mapToRect(Field.mapFromColumn(selectionPath.column), Field.mapFromRow(selectionPath.row), squareSize);
            canvas.drawRect(fRect, hostColor);
            canvas.drawRect(fRect, guestColor);
        }
        hostColor.setPathEffect(null);
        guestColor.setPathEffect(null);
    }

    private List<Field> intersect(List<Field> hostSelection, List<Field> guestSelection) {
        List<Field> result = new ArrayList<>();
        for (Field f : hostSelection) {
            if (guestSelection.contains(f)) {
                result.add(f);
            }
        }
        hostSelection.removeAll(result);
        guestSelection.removeAll(result);
        return result;
    }


    private List<Field> getSelection(Participant p) {
        List<Field> result = new ArrayList<>();
        if (p != null && p.getSelection() != null) {
            Field start = p.getSelection();
            result.add(start);
            List<Field> fields = game.chessGame.board.getSelectionForField(start);
            result.addAll(fields);
        }
        return result;
    }

    private void drawField(int dx, int dy, float squareSize, Canvas canvas) {
        Paint color = (dx + dy) % 2 == 0 ? blackBack : whiteBack;
        Rect fRect = mapToRect(dx, dy, squareSize);
        canvas.drawRect(fRect, color);
        drawFigure(dx, dy, squareSize, canvas);
    }

    private void drawFigure(int dx, int dy, float squareSize, Canvas canvas) {
        if (hasBoard()) {
            Figure figure = findFigureAt(dx, dy);
            if (figure != null) {
                float padding = squareSize / 2f;
                Paint color = figure.color == Color.WHITE ? whiteFigure : blackFigure;
                int xPos = (int) ((dx * squareSize + 0.1f * squareSize) + padding);
                int yPos = (int) (((dy + 1) * squareSize - 0.1f * squareSize) + padding);
                canvas.drawText("" + figure.symbol, xPos, yPos, color);
            }
        }
    }

    private boolean hasBoard() {
        return game != null && game.chessGame != null && game.chessGame.board != null;
    }

    private Figure findFigureAt(int dx, int dy) {
        String column = Field.mapToColumn(dx);
        String row = Field.mapToRow(dy);

        for (Map.Entry<Field, Figure> entry : game.chessGame.board.lineUp.entrySet()) { //NPE already checked
            Field field = entry.getKey();
            if (field.row.equals(row) && field.column.equals(column)) {
                return entry.getValue();
            }
        }
        return null;
    }


    private Rect mapToRect(int column, int row, float squareSize) {
        float padding = squareSize / 2f;
        int x0 = (int) (column * squareSize + padding);
        int y0 = (int) (row * squareSize + padding);
        int x1 = (int) ((column + 1) * squareSize + padding);
        int y1 = (int) ((row + 1) * squareSize + padding);
        return new Rect(x0, y0, x1, y1);
    }
}
