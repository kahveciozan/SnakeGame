package com.kahveci.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;


public class GameScreen extends ScreenAdapter {
    private enum STATE{ PLAYING , GAME_OVER }
    private STATE state = STATE.PLAYING;

    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private Texture snakeHead;
    private Texture apple;
    private Texture snakeBody;

    private static final float MOVE_TIME = 0.1F;
    private float timer = MOVE_TIME;
    private static final int SNAKE_MOVEMENT = 32;

    private int snakeX=0, snakeY=0;
    private int appleX,appleY;
    private int snakeXBeforeUpdate = 0, snakeYBeforeUpdate = 0;
    private boolean appleAvailable = false;

    private static final int RIGHT =0;
    private static final int LEFT =1;
    private static final int UP =2;
    private static final int DOWN =3;
    private int snakeDirection = RIGHT;

    private Array<BodyPart> bodyParts = new Array<BodyPart>();

    private static final int GRID_CELL = 32;

    private boolean directionSet;
    private boolean hasHit = false;                 //collision flag

    private BitmapFont bitmapFont;
    private GlyphLayout layout = new GlyphLayout();
    private static final String GAME_OVER_TEXT = "GAME OVER.. Tap Space to RESTART";

    private int score = 0;
    private static final  int POINTS_PER_APPLE = 20;
    String scoreAsString;

    private static final float WORLD_WIDTH = 640;
    private static final float WORLD_HEIGHT = 480;
    private Viewport viewport;
    private Camera camera;
    @Override
    public void show() {
        batch = new SpriteBatch();
        snakeHead = new Texture(Gdx.files.internal("snakehead.png"));
        apple = new Texture(Gdx.files.internal("apple.png"));
        snakeBody = new Texture(Gdx.files.internal("snakeBody.png"));
        shapeRenderer = new ShapeRenderer();
        bitmapFont = new BitmapFont();


        /*  -   -      - - - - - -BURDA KALDI -------------------------------------------------*/
        camera = new OrthographicCamera(Gdx.graphics.getWidth(),Gdx.graphics.getHeight());      //2ye boollllllllll---------------
        camera.position.set(WORLD_WIDTH,WORLD_HEIGHT,0);
        viewport = new FitViewport(WORLD_WIDTH,WORLD_HEIGHT,camera);
    }

    private void updateSnake(float delta){
        if (!hasHit){
            //Update snake code omitted
        }
        timer -= delta;
        if (timer <= 0){
            timer = MOVE_TIME;
            moveSnake();                            // snake konumunu ve yonunu guncelliyor
            checkForOutBounds();                    // ekrandan cıkmayi ayarliyor
            updateBodyPartsPosition();              // kuyrugun arkdan gelmesini duzenliyor
            checkSnakeBodyCollision();
            directionSet = false;
        }
    }

    @Override
    public void render(float delta) {

        switch (state){
            case PLAYING:{
                queryInput();                               // klavyeden yon bilgisi aliyor
                updateSnake(delta);
                checkAndPlaceApple();                       // yediden apple olusturuyor ( eger uygum ise )
                checkAppleCollection();                     // eslesme oldu ise kuyruga yeni bir parca insert ediyor
            }
            break;
            case GAME_OVER:{
                checkForRestart();
            }
            break;
        }
        clearScreen();
        //drawGrid();                                       // klavuz kareleri cizer
        draw();

    }

    private void checkForOutBounds(){
        if (snakeX >= Gdx.graphics.getWidth()){
            snakeX = 0;
        }
        if (snakeX < 0){
            snakeX = Gdx.graphics.getWidth()-SNAKE_MOVEMENT;
        }
        if (snakeY >= Gdx.graphics.getHeight()){
            snakeY = 0;
        }
        if (snakeY < 0){
            snakeY = Gdx.graphics.getHeight()-SNAKE_MOVEMENT;
        }
    }

    private void moveSnake(){
        snakeXBeforeUpdate = snakeX;
        snakeYBeforeUpdate = snakeY;

        switch (snakeDirection){
            case RIGHT:{
                snakeX += SNAKE_MOVEMENT;
                return;
            }
            case LEFT:{
                snakeX -=SNAKE_MOVEMENT;
                return;
            }
            case UP:{
                snakeY += SNAKE_MOVEMENT;
                return;
            }
            case DOWN:{
                snakeY -= SNAKE_MOVEMENT;
                return;
            }
        }
    }

    private void queryInput(){
        boolean lPressed = Gdx.input.isKeyPressed(Input.Keys.LEFT);
        boolean rPressed = Gdx.input.isKeyPressed(Input.Keys.RIGHT);
        boolean uPressed = Gdx.input.isKeyPressed(Input.Keys.UP);
        boolean dPressed = Gdx.input.isKeyPressed(Input.Keys.DOWN);

        if (lPressed) updateDirection(LEFT);
        if (rPressed) updateDirection(RIGHT);
        if (uPressed) updateDirection(UP);
        if (dPressed) updateDirection(DOWN);
    }

    private void checkAndPlaceApple(){
        if (!appleAvailable){
            do {
                appleX = MathUtils.random(Gdx.graphics.getWidth() / SNAKE_MOVEMENT-1) * SNAKE_MOVEMENT;
                appleY = MathUtils.random(Gdx.graphics.getHeight() / SNAKE_MOVEMENT-1) * SNAKE_MOVEMENT;
                appleAvailable=true;
            }while (appleX == snakeX && appleY == snakeY);
        }
    }

    private void clearScreen(){
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    private void draw(){
        batch.begin();

        batch.draw(snakeHead,snakeX,snakeY);
        for (BodyPart bodyPart : bodyParts){
            bodyPart.draw(batch);
        }

        if (appleAvailable){
            batch.draw(apple,appleX,appleY);
        }

        if (state == STATE.GAME_OVER){
            layout.setText(bitmapFont,GAME_OVER_TEXT);
            bitmapFont.draw(batch,GAME_OVER_TEXT, (Gdx.graphics.getWidth()-layout.width)/2, (Gdx.graphics.getHeight()-layout.height)/2);
        }

        drawScore();
        batch.end();
    }

    private void checkAppleCollection(){
        if (appleAvailable && appleX == snakeX && appleY == snakeY){
            BodyPart bodyPart = new BodyPart(snakeBody);
            bodyPart.updateBodyPosition(snakeX,snakeY);
            //0. imdekse yeni bir boyPart sikistiriyor !!Onceden olan mevcut bulunan bodyPart'lar bir ust indekse yerlesiyor
            bodyParts.insert(0,bodyPart);
            addToScore();
            appleAvailable = false;
        }
    }

    private void updateBodyPartsPosition(){
        if (bodyParts.size > 0){
            BodyPart bodyPart = bodyParts.removeIndex(0);                                   // en uzaktaki parcayi aliyor
            bodyPart.updateBodyPosition(snakeXBeforeUpdate,snakeYBeforeUpdate);             // konumunu en yakın olarak guncelliyor
            bodyParts.add(bodyPart);                                                        // ve tekar ekrana basiyor ( en yakina )
        }
    }

    private void drawGrid(){
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        for (int x = 0; x<Gdx.graphics.getWidth(); x+= GRID_CELL){
            for (int y = 0; y<Gdx.graphics.getHeight(); y+= GRID_CELL){
                shapeRenderer.rect(x,y,GRID_CELL,GRID_CELL);
                shapeRenderer.circle(x,y,2);
            }
        }
        shapeRenderer.setColor(Color.LIGHT_GRAY);
        shapeRenderer.end();
    }

    private void updateIfNotOppositeDirection( int newSnakeDirection, int oppositeDirection){
        if (snakeDirection != oppositeDirection || bodyParts.size == 0 ){
            snakeDirection = newSnakeDirection;
        }
    }

    private void updateDirection(int newSnakeDirection){
        if (!directionSet && snakeDirection != newSnakeDirection){
            directionSet = true;
            switch (newSnakeDirection){
                case LEFT:{
                    updateIfNotOppositeDirection(newSnakeDirection, RIGHT);
                }
                break;
                case RIGHT: {
                    updateIfNotOppositeDirection(newSnakeDirection,LEFT);
                }
                break;
                case UP: {
                    updateIfNotOppositeDirection(newSnakeDirection,DOWN);
                }
                break;
                case DOWN: {
                    updateIfNotOppositeDirection(newSnakeDirection,UP);
                }
                break;
            }
        }
    }

    private void checkSnakeBodyCollision(){
        for (BodyPart bodyPart : bodyParts){
            if (bodyPart.x == snakeX && bodyPart.y == snakeY)
                state = STATE.GAME_OVER;
                //hasHit = true;
        }
    }

    private void checkForRestart(){
        if (Gdx.input.isKeyPressed(Input.Keys.SPACE))
            doRestart();
    }

    private void doRestart(){
        state = STATE.PLAYING;
        bodyParts.clear();
        snakeDirection = RIGHT;
        directionSet = false;
        timer = MOVE_TIME;
        snakeX = 0; snakeY = 0;
        snakeXBeforeUpdate =0; snakeYBeforeUpdate = 0;
        appleAvailable = false;
        score = 0;
    }

    private void addToScore(){
        score += POINTS_PER_APPLE;
    }

    private void drawScore(){
        if (state == STATE.PLAYING){
            scoreAsString = Integer.toString(score);
        }
        else if (state == STATE.GAME_OVER){
            scoreAsString = "YOUR SCORE: "+score;
        }
        layout.setText(bitmapFont,scoreAsString);
        bitmapFont.draw(batch,scoreAsString, ((Gdx.graphics.getWidth()-layout.width)/2), (float) ((Gdx.graphics.getHeight()-layout.height)/1.5));

    }

    // Yilanin Kuyrugu -- INNER CLASS --
    private class BodyPart{
        private int x,y;
        private Texture texture;

        public BodyPart(Texture texture){
            this.texture = texture;
        }

        public void updateBodyPosition(int x,int y){
            this.x = x;
            this.y = y;
        }

        public void draw(Batch batch){
            if (!(x == snakeX && y == snakeY)){
                batch.draw(texture,x,y);
            }
        }
    }
}