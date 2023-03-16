package com.github.martinfrank.chessapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import androidx.annotation.Nullable;
import ch.qos.logback.core.util.SystemInfo;
import com.github.martinfrank.games.chessmodel.model.Game;
import com.github.martinfrank.games.chessmodel.model.GameContent;
import com.github.martinfrank.games.chessmodel.model.chess.Board;
import com.github.martinfrank.games.chessmodel.model.chess.Color;
import com.github.martinfrank.games.chessmodel.model.chess.Field;
import com.github.martinfrank.games.chessmodel.model.chess.Figure;

import java.util.Map;

public class ChessBoardView extends View {

    private static final String LOG_TAG = "ChessBoardView";

    private int viewSizeInPixels;
    private final Paint blackBack = new Paint();
    private final Paint whiteBack = new Paint();
    private final Paint blackFigure = new Paint();
    private final Paint whiteFigure = new Paint();
    private final Paint hostColor  = new Paint();
    private final Paint guestColor = new Paint();
    private Game game;

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

        hostColor.setStyle(Paint.Style.STROKE);
        guestColor.setStyle(Paint.Style.STROKE);
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
        if(hasBoard()){
            drawSelection(canvas, squareSize);
        }
    }

    private void drawSelection(Canvas canvas, double squareSize) {
        Field hostSelection = game.gameContent.getHostSelection();
        if(hostSelection != null){
            int x0 = (int)(Field.mapFromColumn(hostSelection.column) * squareSize);
            int y0 = (int)(Field.mapFromRow(hostSelection.row) * squareSize);
            int x1 = (int)((Field.mapFromColumn(hostSelection.column)+1) * squareSize);
            int y1 = (int)((Field.mapFromRow(hostSelection.row)+1) * squareSize);
            Rect field = new Rect(x0,y0,x1,y1);
            canvas.drawRect(field, hostColor);
        }

    }

    private void drawField(int dx, int dy, float squareSize, Canvas canvas) {
        Paint color = (dx + dy) % 2 == 0 ? blackBack : whiteBack;
        Rect field = new Rect((int) (dx * squareSize), (int) (dy * squareSize), (int) ((dx + 1) * squareSize), (int) ((dy + 1) * squareSize));
        canvas.drawRect(field, color);
        drawFigure(dx, dy, squareSize, canvas);
    }

    private void drawFigure(int dx, int dy, float squareSize, Canvas canvas) {
        if (hasBoard()) {
            Figure figure = findFigureAt(dx, dy);
            if (figure != null) {
                Paint color = figure.color == Color.WHITE ? whiteFigure : blackFigure;
                canvas.drawText("" + figure.symbol, (int)(dx * squareSize+0.1f*squareSize), (int)((dy+1) * squareSize-0.1f*squareSize), color);
            }
        }
    }

    private boolean hasBoard() {
        return game != null && game.gameContent != null && game.gameContent.board != null;
    }

    private Figure findFigureAt(int dx, int dy) {
        String column = Field.mapToColumn(dx);
        String row = Field.mapToRow(dy);

        for (Map.Entry<Field, Figure> entry : game.gameContent.board.lineUp.entrySet()) { //NPE already checked
            Field field = entry.getKey();
            if (field.row.equals(row) && field.column.equals(column)) {
                return entry.getValue();
            }
        }
        return null;
    }


    public void updateBoard(Game game) {
        this.game = game;
        int hr = (game.hostPlayer.color & 0xFF0000 ) >> 16;
        int hg = (game.hostPlayer.color & 0x00FF00) >> 8;
        int hb = game.hostPlayer.color & 0x0000FF;
        hostColor.setARGB(0xff, 0,0xFF,0);
        if(game.getGuestPlayer() != null){
            int gr = (game.getGuestPlayer().color & 0xFF0000 ) >> 16;
            int gg = (game.getGuestPlayer().color & 0x00FF00) >> 8;
            int gb = game.getGuestPlayer().color & 0x0000FF;
            guestColor.setARGB(0xff, gr,gg,gb);
        }
    }


    private void setFigure(Field field, Figure figure) {

    }

    public Field getFieldAt(float x, float y) {
        double rasterSize = viewSizeInPixels/8f;
        int xInt = (int)(x / rasterSize);
        int yInt = (int)(y / rasterSize);
        Log.d(LOG_TAG, "mapping touch: "+x+"/"+y+" to field at: "+xInt+"/"+yInt);
        String column = Field.mapToColumn(xInt);
        String row = Field.mapToRow(yInt);
        return new Field(row, column);
    }
}
