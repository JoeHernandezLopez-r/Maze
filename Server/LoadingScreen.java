package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch; 
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.TimeUtils;

public class LoadingScreen implements Screen {

    private final MyGdxGame game;
    private BatchManager batchManager;
    private SpriteBatch spriteBatch;
    private Texture defaultTexture;
    private long startTime;  // Time when loading started
    private float loadingDuration = 5.0f;  // Loading duration in seconds
    private float loadingProgress = 0.0f;  // Loading progress

    public LoadingScreen(MyGdxGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        batchManager = new BatchManager();
        batchManager.initialize();

         // Initialization for the screen
         spriteBatch = batchManager.getBatch();

         // Load default libGDX texture (replace "badlogic.jpg" with your actual file)
         defaultTexture = new Texture("badlogic.jpg");

        startTime = TimeUtils.millis();  // Record the start time
    }

    @Override
    public void render(float delta) {
        // Calculate elapsed time since loading started
        float elapsedSeconds = (TimeUtils.millis() - startTime) / 1000.0f;

        // Update loading progress based on elapsed time
        loadingProgress = Math.min(elapsedSeconds / loadingDuration, 1.0f);

        // You can also render a loading bar or percentage on the screen
        // For simplicity, let's print the progress to the console
        System.out.println("Loading Progress: " + loadingProgress);
        // Clear the screen
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Begin sprite batch
        spriteBatch.begin();

        // Render the default texture at position (0, 0)
        spriteBatch.draw(defaultTexture, 0, 0);

        // End sprite batch
        spriteBatch.end();
        // If loading is complete, you can switch to the next screen
        if (loadingProgress >= 1.0f) {
            game.setScreen(new MainScreen(game, batchManager));
        }
    }

    @Override
    public void resize(int width, int height) {
        // Handle screen resizing
    }

    @Override
    public void pause() {
        // Handle game pause, if needed
    }

    @Override
    public void resume() {
        // Handle game resume, if needed
    }

    @Override
    public void hide() {
        // This method is called when the screen is no longer the current screen
    }

    @Override
    public void dispose() {
        // Cleanup resources here
        defaultTexture.dispose();
    }
}
