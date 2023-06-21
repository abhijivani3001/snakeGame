package MainPackage;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class PlayAudio extends Thread{
    String audioName;
    boolean audioType;

    private Media audio2;
    private void playAudio(){
        try{
            audio2 =  new Media(this.getClass().getResource("/Audio/"+audioName+".mp3").toExternalForm());
            MediaPlayer mediaPlayer = new MediaPlayer(audio2);
            if(this.audioType) mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE); // To repeat the song
            mediaPlayer.play();

            while (!Thread.currentThread().isInterrupted() && audioType) {}
            if(!audioType) Thread.sleep(1000); // Sleep for 1000ms(1 Sec) and then it will stop the audio which has type false
            mediaPlayer.stop();
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    PlayAudio(String audioName, boolean audioType){
        this.audioName=audioName;
        this.audioType=audioType;
    }

    @Override
    public void run(){
        this.playAudio();
    }
}
