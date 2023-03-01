package com.github.martinfrank.chessapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;
import com.github.martinfrank.games.chessmodel.model.GameContent;
import com.github.martinfrank.games.chessmodel.model.chess.Board;
import com.github.martinfrank.games.chessmodel.model.chess.Color;
import com.github.martinfrank.games.chessmodel.model.chess.Field;
import com.github.martinfrank.games.chessmodel.model.chess.Figure;

import java.util.Map;

public class ChessBoardView extends View {

    private int viewSizeInPixels;
    private final Paint blackBack = new Paint();
    private final Paint whiteBack = new Paint();
    private final Paint blackFigure = new Paint();
    private final Paint whiteFigure = new Paint();
    private Board board;

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

        blackFigure.setARGB(0xff, 0xff,0,0);
        blackFigure.setStyle(Paint.Style.FILL);
        whiteFigure.setARGB(0xff, 0xFF,0xFF,0xFF);
        whiteFigure.setStyle(Paint.Style.FILL);
    }


    @Override
    protected void onMeasure(int width, int height) {
        super.onMeasure(width, height);
        //make it square-shaped
        int measuredWidth = getMeasuredWidth();
        int measuredHeight = getMeasuredHeight();
        viewSizeInPixels = Math.min(measuredWidth, measuredHeight);
        setMeasuredDimension(viewSizeInPixels, viewSizeInPixels);
        blackFigure.setTextSize(viewSizeInPixels / 8f);
        whiteFigure.setTextSize(viewSizeInPixels / 8f);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float squareSize = viewSizeInPixels / 8f;
        drawFields(canvas, squareSize);
    }

    private void drawFields(Canvas canvas, float squareSize) {
        for (int dy = 0; dy < 8; dy++) {
            for (int dx = 0; dx < 8; dx++) {
                drawField(dx, dy, squareSize, canvas);
            }
        }
    }

    private void drawField(int dx, int dy, float squareSize, Canvas canvas) {
        Paint color = (dx + dy) % 2 == 0 ? blackBack : whiteBack;
        Rect field = new Rect((int) (dx * squareSize), (int) (dy * squareSize), (int) ((dx + 1) * squareSize), (int) ((dy + 1) * squareSize));
        canvas.drawRect(field, color);
        drawFigure(dx, dy, squareSize, canvas);
    }

    private void drawFigure(int dx, int dy, float squareSize, Canvas canvas) {
        if (board != null) {
            Figure figure = findFigureAt(dx, dy);
            if (figure != null) {
                Paint color = figure.color == Color.WHITE ? whiteFigure : blackFigure;
                canvas.drawText("" + figure.symbol, (int)(dx * squareSize+0.1f*squareSize), (int)((dy+1) * squareSize-0.1f*squareSize), color);
            }
        }
    }

    private Figure findFigureAt(int dx, int dy) {
        String column = Field.mapToColumn(dx);
        String row = Field.mapToRow(dy);

        for (Map.Entry<Field, Figure> entry : board.lineUp.entrySet()) { //NPE already checked
            Field field = entry.getKey();
            if (field.row.equals(row) && field.column.equals(column)) {
                return entry.getValue();
            }
        }
        return null;
    }


    public void updateBoard(GameContent gameContent) {
        this.board = gameContent.board;
//        for (Map.Entry<Field, Figure> entry : gameContent.board.lineUp.entrySet()) {
//            Field field = entry.getKey();
//            Figure figure = entry.getValue();
//            setFigure(field, figure);
//        }
    }

    private void setFigure(Field field, Figure figure) {

    }
}
