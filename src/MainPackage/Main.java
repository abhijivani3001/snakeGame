package MainPackage;

import javafx.animation.*;
import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.event.*;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

import java.awt.*;
import java.io.*;
import java.util.*;

public class Main extends Application {

    private static final int WIDTH=700;
    private static final int HEIGHT=700;
    private static final int ROWS=20;
    private static final int COLUMNS=20;
    private static final int SQUARE_SIZE=WIDTH/ROWS; // 35
    private static final String[] LOGO_IMAGE=new String[]{"/Images/Apple.png", "/Images/Grapes.png", "/Images/Cherry.png", "/Images/Mango.png", "/Images/Orange.png"};

    private static final int RIGHT=0;
    private static final int LEFT=1;
    private static final int UP=2;
    private static final int DOWN=3;

    private GraphicsContext gc;
    private ArrayList<Point> snakeBody=new ArrayList<>();
    private Point snakeHead;
    private Image logoImage;
    private int logoX;
    private int logoY;
    private boolean gameOver=false;
    private int currentDirection=0; // Right Direction
    private int score=0;
    private int normalPointValue=5;
    private int bonusPointValue=5;
    private int countBonusPoints=0;
    private int eatenLogos=0;
    private final int timelineDuration=120; // MilliSeconds
    Rectangle2D screenBounds = Screen.getPrimary().getBounds(); // To check size of Screen: Rectangle2D [minX = 0.0, minY=0.0, maxX=1920.0, maxY=1080.0, width=1920.0, height=1080.0]

    Timeline timeline;
    private boolean flagForBigLogo=true;
    private boolean flagForTimeline=true;
    private boolean flagForHighScore=false;

    private long pauseTime=0, playTime=0;
    BigLogo bigLogo;

    private int startX=(int)screenBounds.getMaxX()/2-(WIDTH/2);
    private int startY=(int)screenBounds.getMaxY()/2-(HEIGHT/2)+20;

    private double bufferX=2.0, bufferY=2.5;

    private String playerName;
    private static String fileName="src/MainPackage/scores.txt";

    PlayAudio introSong;
    PlayAudio mainSong;
    PlayAudio keyPressedSound, clickRightSound, clickLeftSound, clickUpSound, clickDownSound;
    PlayAudio buzzerSound;
    PlayAudio eatFruitSound;
    PlayAudio bonusPointSound;



    @Override
    public void start(Stage primaryStage) throws Exception {
        introSong=new PlayAudio("IntroSong",true);
        introSong.start();

//        System.out.println(startX); // 418
//        System.out.println(startY); // 102
//        System.out.println((startX/SQUARE_SIZE) + 1 +(int)(0.999999*ROWS)); //[12,31] [minPosOfX for Logo, maxPosOfX for Logo]
//        System.out.println((startY/SQUARE_SIZE) + 1 +(int)(0.999999*COLUMNS)); //[3,22] [minPosOfY for Logo, maxPosOfY for Logo]

        primaryStage.setTitle("Snake Game");
        Group root=new Group();
        Canvas canvas=new Canvas(screenBounds.getWidth(), screenBounds.getHeight());
        root.getChildren().add(canvas);
        Scene scene=new Scene(root,Color.web("#008080"));
        primaryStage.getIcons().add(new Image("/Images/SnakeIcon.jpg"));

        gc=canvas.getGraphicsContext2D();

        gc.setFill(Color.web("#000223"));
        gc.setFont(Font.font("Comic Sans MS", FontWeight.BOLD,140));
        gc.fillText("Snake Game",screenBounds.getWidth()/4.3,screenBounds.getHeight()/3.5);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Comic Sans MS", FontPosture.ITALIC,70));
        gc.fillText("Enter SPACE to start the game",screenBounds.getWidth()/5.9,screenBounds.getHeight()/1.6);
        gc.fillText("Enter ESC to exit the game",screenBounds.getWidth()/4.8,screenBounds.getHeight()/1.3);

        primaryStage.setMaximized(true); // To set Maximized Screen

        primaryStage.setResizable(false);
        primaryStage.setScene(scene);
        primaryStage.show();

        for(int i=3;i>0;i--){
            snakeBody.add(new Point(i+(COLUMNS/2)+3,ROWS/2)); // Snake start running from (x, y) coordinate
        }
        snakeHead=snakeBody.get(0);
        generateLogo();

        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent windowEvent) {
                System.exit(0);
            }
        });

        scene.setOnKeyPressed (new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                KeyCode code=keyEvent.getCode();
                if(code==KeyCode.ESCAPE){
                    System.exit(0);
                }
                else if(code==KeyCode.SPACE){
                    introSong.interrupt(); // Intro Song Stopped

                    keyPressedSound=new PlayAudio("ClickButton",false);
                    keyPressedSound.start();

                    mainSong=new PlayAudio("BaraBere",true);
                    mainSong.start();

                    temprun(gc,scene,primaryStage);
                }
            }
        });

    }

    public void temprun(GraphicsContext gc,Scene scene,Stage stage){
        gc.setFill(Color.BLACK);
        gc.fillRect(0,0,screenBounds.getMaxX(),screenBounds.getMaxY());

        timeline=new Timeline(new KeyFrame(Duration.millis(timelineDuration),e->run(gc,scene,stage)));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    public void run(GraphicsContext gc,Scene scene,Stage stage){

        keyPressedSound=new PlayAudio("ClickButton",false);
        clickLeftSound=new PlayAudio("ClickButton",false);
        clickRightSound=new PlayAudio("ClickButton",false);
        clickUpSound=new PlayAudio("ClickButton",false);
        clickDownSound=new PlayAudio("ClickButton",false);


        scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {

                KeyCode code=keyEvent.getCode();

                if(code==KeyCode.ESCAPE){
                    System.exit(0);
                }
                else if(code==KeyCode.SPACE){
                    if(flagForTimeline){
                        timeline.pause();
                        flagForTimeline=false;
                        pauseTime=System.currentTimeMillis(); // Takes time when user pause the game
                    }
                    else{
                        timeline.play();
                        flagForTimeline=true;
                        playTime=System.currentTimeMillis(); // Takes the time when user resume the game
                    }
                }
                if(flagForTimeline) {
                    if (code == KeyCode.RIGHT || code == KeyCode.D) {
                        if (currentDirection != LEFT && currentDirection != RIGHT) {
                            clickRightSound.start();
                            currentDirection = RIGHT; // 0
                        }
                    }
                    else if (code == KeyCode.LEFT || code == KeyCode.A) {
                        if (currentDirection != RIGHT && currentDirection != LEFT) {
                            clickLeftSound.start();
                            currentDirection = LEFT; // 1
                        }
                    }
                    else if (code == KeyCode.UP || code == KeyCode.W) {
                        if (currentDirection != DOWN && currentDirection != UP) {
                            clickUpSound.start();
                            currentDirection = UP; // 2
                        }
                    }
                    else if (code == KeyCode.DOWN || code == KeyCode.S) {
                        if (currentDirection != UP && currentDirection != DOWN) {
                            clickDownSound.start();
                            currentDirection = DOWN; // 3
                        }
                    }
                }
            }
        });


//        To Move Snake Body(!Head): It decreases the positions of each node(Body Part) towards Head
        for(int i=snakeBody.size()-1;i>=1;i--){
            snakeBody.get(i).x=snakeBody.get(i-1).x;
            snakeBody.get(i).y=snakeBody.get(i-1).y;
        }

        switch (currentDirection){
            case RIGHT:
                moveRight();
                break;
            case LEFT:
                moveLeft();
                break;
            case UP:
                moveUp();
                break;
            case DOWN:
                moveDown();
                break;
        }


        eatLogo();
        gameOver();

        if(gameOver){
            writeScoreToFile();

            buzzerSound=new PlayAudio("BuzzerSound",false);
            buzzerSound.start();

            mainSong.interrupt();

            timeline.stop();
            drawGameOverBackground(gc);

            playTime=0;
            pauseTime=0;

            scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
                @Override
                public void handle(KeyEvent keyEvent) {
                    if(keyEvent.getCode()==KeyCode.ESCAPE){
                        System.exit(0);
                    }
                    else if(keyEvent.getCode()==KeyCode.SPACE || keyEvent.getCode()==KeyCode.ENTER){
                        try {
                            keyPressedSound.start();
                            eatenLogos=0;
                            score=0;
                            countBonusPoints=0;
                            gameOver=false;
                            flagForHighScore=false;
                            currentDirection=0; // Right
                            snakeBody.clear();
                            start(stage);
                        }
                        catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            });
            return;
        }

//        This Order should be maintained:
        drawMainBackground(gc);
        drawLogo(gc);
        drawScore(gc);
        drawSnake(gc);
    }

    private void drawMainBackground(GraphicsContext gc){

        for(int i=0;i<ROWS;i++){
            for(int j=0;j<COLUMNS;j++){
                if(((i+j)&1)==0)
                    gc.setFill(Color.web("#BDFFF6")); // HEX color code(in String)
                else
                    gc.setFill(Color.web("#317874"));
                gc.fillRect(i*SQUARE_SIZE+startX,j*SQUARE_SIZE+startY,SQUARE_SIZE,SQUARE_SIZE);
            }
        }

        gc.setFill(Color.BLACK);
        gc.fillRect((startX-SQUARE_SIZE),(startY-SQUARE_SIZE),(COLUMNS+2)*SQUARE_SIZE,SQUARE_SIZE);
        gc.fillRect(startX-SQUARE_SIZE,startY+(ROWS)*SQUARE_SIZE,(COLUMNS+2)*SQUARE_SIZE,SQUARE_SIZE);
        gc.fillRect((startX-SQUARE_SIZE),(startY-SQUARE_SIZE),SQUARE_SIZE,(ROWS+2)*SQUARE_SIZE);
        gc.fillRect(startX+(COLUMNS)*SQUARE_SIZE,(startY-SQUARE_SIZE),SQUARE_SIZE,(ROWS+2)*SQUARE_SIZE);
    }

    private void generateLogo(){
        flag:
        while (true){
            logoX=(int)(Math.random()*ROWS + (startX/SQUARE_SIZE) + 1);
            logoY=(int)(Math.random()*COLUMNS + (startY/SQUARE_SIZE) + 1);

//            Check for Generated Logo is not at the location of Snake Body
            for(Point snake: snakeBody){
                if(snake.getX()==logoX && snake.getY()==logoY){
                    continue flag;
                }
            }
            logoImage=new Image(LOGO_IMAGE[(int) (Math.random()*LOGO_IMAGE.length)]);
            break;
        }
    }

    private void drawLogo(GraphicsContext gc) {

        if(eatenLogos%5==0 && score!=0 && flagForBigLogo) {
            if (bigLogo == null)
                bigLogo = new BigLogo();

            if (logoX == 12) logoX++;
            if (logoY == 3) logoY++;
            if (logoX == COLUMNS - 1 + 12) logoX--;
            if (logoY == ROWS - 1 + 3) logoY--;

            if (System.currentTimeMillis() - bigLogo.time <= 5000 + (playTime - pauseTime)) {
                gc.drawImage(logoImage, (logoX*SQUARE_SIZE)-(SQUARE_SIZE/3)-bufferX, logoY*SQUARE_SIZE-SQUARE_SIZE/3-bufferY, SQUARE_SIZE*(5.0/3), SQUARE_SIZE*(5.0/3));
            }
            else{
                gc.drawImage(logoImage, logoX*SQUARE_SIZE-bufferX, logoY*SQUARE_SIZE-bufferY, SQUARE_SIZE, SQUARE_SIZE);
                bigLogo=null;
                flagForBigLogo =false;
                playTime=0;
                pauseTime=0;
            }
        }
        else{
            gc.drawImage(logoImage, logoX*SQUARE_SIZE-bufferX, logoY*SQUARE_SIZE-bufferY, SQUARE_SIZE, SQUARE_SIZE);
            bigLogo=null;
            playTime=0;
            pauseTime=0;
        }
    }

    private void drawSnake(GraphicsContext gc){
        gc.setFill(Color.web("#E72660"));

//        Syntax: fillRoundRect(startingPositionOfX, startingPositionOfY, Width, Height, RadiusOfX, RadiusOfY);

//        Snake Head:
//        gc.fillRoundRect(snakeHead.getX()*SQUARE_SIZE- bufferX,snakeHead.getY()*SQUARE_SIZE- bufferY,SQUARE_SIZE-1,SQUARE_SIZE-1,35,35);
        if(currentDirection==RIGHT) gc.drawImage(new Image("Images/SnakeHeadRight.png"),snakeHead.getX()*SQUARE_SIZE-bufferX,snakeHead.getY()*SQUARE_SIZE-bufferY,SQUARE_SIZE*1.6,SQUARE_SIZE);
        else if(currentDirection==LEFT) gc.drawImage(new Image("Images/SnakeHeadLeft.png"),snakeHead.getX()*SQUARE_SIZE-bufferX -0.63*SQUARE_SIZE,snakeHead.getY()*SQUARE_SIZE-bufferY,SQUARE_SIZE*1.6,SQUARE_SIZE);
        else if(currentDirection==UP) gc.drawImage(new Image("Images/SnakeHeadUp.png"),snakeHead.getX()*SQUARE_SIZE-bufferX,snakeHead.getY()*SQUARE_SIZE-bufferY - 0.63*SQUARE_SIZE,SQUARE_SIZE,SQUARE_SIZE*1.6);
        else if(currentDirection==DOWN) gc.drawImage(new Image("Images/SnakeHeadDown.png"),snakeHead.getX()*SQUARE_SIZE-bufferX,snakeHead.getY()*SQUARE_SIZE-bufferY,SQUARE_SIZE,SQUARE_SIZE*1.6);

        for(int i=1;i<snakeBody.size();i++){
//            Snake Body:
            gc.fillRoundRect(snakeBody.get(i).getX()*SQUARE_SIZE- bufferX,snakeBody.get(i).getY()*SQUARE_SIZE- bufferY,SQUARE_SIZE-1,SQUARE_SIZE-1,20,20);
        }

    }

    private void moveRight(){
        snakeHead.x++;
    }
    private void moveLeft(){
        snakeHead.x--;
    }
    private void moveUp(){
        snakeHead.y--;
    }
    private void moveDown(){
        snakeHead.y++;
    }

    public void gameOver(){
//        Hit to the Border:
        if(snakeHead.x<12 || snakeHead.y<3 || snakeHead.x>=COLUMNS+12 || snakeHead.y>=ROWS+3){
            gameOver=true;
        }

//        Destroy itself:
        for(int i=1;i<snakeBody.size();i++){
            if(snakeHead.x==snakeBody.get(i).getX() && snakeHead.y==snakeBody.get(i).getY()){
                gameOver=true;
                break;
            }
        }
    }

    private void eatLogo(){
        eatFruitSound=new PlayAudio("EatingFruit",false);
        bonusPointSound=new PlayAudio("BonusPoint",false);

        if(snakeHead.getX()==logoX && snakeHead.getY()==logoY){
            generateLogo();
            snakeBody.add(new Point(-1,-1));  // generates new square at (-1,-1) position

            if(eatenLogos%5==0 && score!=0 && flagForBigLogo){
                bonusPointSound.start();
                score+=normalPointValue+bonusPointValue;
                countBonusPoints++;
            }
            else {
                eatFruitSound.start();
                score+=normalPointValue;
            }
            flagForBigLogo=true;
            eatenLogos++;
        }
    }

    private void drawScore(GraphicsContext gc){
        gc.setFill(Color.BLACK);
        gc.fillRect(0,0,screenBounds.getWidth(),90);
        gc.setFill(Color.WHITE);
        gc.setFont(new Font("Comic Sans MS",50));
        gc.fillText("Score: "+score,screenBounds.getWidth()/2.3,65);
    }

    private void drawGameOverBackground(GraphicsContext gc){
        gc.setFill(Color.BLACK);
        gc.fillRect(0,0,screenBounds.getWidth(),screenBounds.getHeight());

        if(flagForHighScore){
            gc.setFill(Color.web("#449e48"));
            gc.setFont(Font.font("Comic Sans MS", FontWeight.NORMAL,60));
            gc.fillText("High Score", screenBounds.getWidth()/2.5,screenBounds.getHeight()/2.5);
        }

        gc.setFill(Color.WHITE);
        gc.setFont(new Font("Comic Sans MS",50));
        gc.fillText("Score: "+score,screenBounds.getWidth()/2.3,65);

        gc.setFill(Color.web("#FF2C2C"));
        gc.setFont(Font.font("Comic Sans MS", FontWeight.BOLD,140));
        gc.fillText("Game Over",screenBounds.getWidth()/3.8,screenBounds.getHeight()/3.3);

        gc.setFill(Color.web("00BFFF"));
        gc.setFont(Font.font("Comic Sans MS", FontPosture.ITALIC,60));
        gc.fillText("Enter SPACE/ENTER to Play Again",screenBounds.getWidth()/5.6,screenBounds.getHeight()/1.6);
        gc.fillText("Enter ESC to exit the game",screenBounds.getWidth()/4,screenBounds.getHeight()/1.3);
    }



//    private void gameEnded(){
//        List<HighScore> highScores=HighScore.getHighScores();
//        if(highScores.isEmpty() || score>highScores.get(0).getScore()){
//            TextInputDialog dialog=new TextInputDialog();
//            dialog.setTitle("High Score");
//            dialog.setHeaderText("Congo!!!");
//            dialog.setContentText("Enter your name: ");
//            dialog.showAndWait().ifPresent(name->{
//                playerName=name;
//                HighScore newScore=new HighScore(playerName,score);
//                HighScore.updateHighScores(newScore);
//            });
//            showHighScore();
//        }
//    }
//    private void showHighScore(){
//        List<HighScore> highScores=HighScore.getHighScores();
//        StringBuilder message=new StringBuilder("High Scores:\n");
//        for(HighScore highScore:highScores){
//            message.append(highScore.getPlayerName()).append(": ").append(highScore);
//        }
//        Alert alert = new Alert(Alert.AlertType.INFORMATION);
//        alert.setTitle("High Scores");
//        alert.setHeaderText(null);
//        alert.setContentText(message.toString());
//        alert.showAndWait();
//    }


    void writeScoreToFile(){
        readFile(fileName);
        try(FileWriter writer=new FileWriter(fileName,true)){
            writer.write(score+"\n");
            System.out.println("Score added: "+score);
        }
        catch (IOException e) {
//            throw new RuntimeException(e);
            System.out.println("Error writing score file: " + e.getMessage());
        }
    }

    private void readFile(String file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if(score<=Integer.parseInt(line)){
//                    System.out.println("less than");
                    flagForHighScore=false;
                    return;
                }
            }
//            System.out.println("high");
            flagForHighScore=true;
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
