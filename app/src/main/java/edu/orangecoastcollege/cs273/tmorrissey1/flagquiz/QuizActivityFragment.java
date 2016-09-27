package edu.orangecoastcollege.cs273.tmorrissey1.flagquiz;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;


/**
 * A placeholder fragment containing a simple view.
 */
public class QuizActivityFragment extends Fragment {

    private static final String TAG = "FlagQuiz Activity";

    private static  final int FLAGS_IN_QUIZ = 10;

    private List<String> fileNameList;
    private List<String> quizCountriesList;
    private Set<String> regionsSet;
    private String correctAnswer;
    private int totalGuesses;
    private int correctAnswers;
    private int guessRows;
    private SecureRandom random;
    private Handler handler;

    private TextView questionNumberTextView;
    private ImageView flagImageView;
    private LinearLayout[] guessLinearLayouts;
    private TextView answerTextView;


    public QuizActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_quiz, container, false);

        fileNameList = new ArrayList<>();
        quizCountriesList = new ArrayList<>();
        random = new SecureRandom();
        handler = new Handler();

        questionNumberTextView = (TextView) view.findViewById(R.id.questionNumberTextView);
        flagImageView = (ImageView) view.findViewById(R.id.flagImageView);
        guessLinearLayouts = new LinearLayout[4];
        guessLinearLayouts[0] = (LinearLayout) view.findViewById(R.id.row1LinearLayout);
        guessLinearLayouts[1] = (LinearLayout) view.findViewById(R.id.row2LinearLayout);
        guessLinearLayouts[2] = (LinearLayout) view.findViewById(R.id.row3LinearLayout);
        guessLinearLayouts[3] = (LinearLayout) view.findViewById(R.id.row4LinearLayout);
        answerTextView = (TextView) view.findViewById(R.id.answerTextView);

        // Configure listeners for the guess buttons
        for (LinearLayout row : guessLinearLayouts) {
            int childCount = row.getChildCount();

            for (int column = 0; column < childCount; ++column) {
                Button button = (Button) row.getChildAt(column);
                button.setOnClickListener(guessButtonListener);
            }
        }

        questionNumberTextView.setText(getString(R.string.question, 1, FLAGS_IN_QUIZ));
        return view;
    }

    public void updateGuessRows(SharedPreferences sharedPreferences) {
        // Get number of guess rows that should be displayed
        String choices = sharedPreferences.getString(QuizActivity.CHOICES, null);
        guessRows = Integer.parseInt(choices) / 2;

        // Hide all guess button linear layouts
        for (LinearLayout layout : guessLinearLayouts)
            layout.setVisibility(View.GONE);

        // Display appropriate guess button LinearLayouts
        for (int row = 0; row < guessRows; ++row)
            guessLinearLayouts[row].setVisibility(View.VISIBLE);
    }

    public void updateRegions(SharedPreferences sharedPreferences) {
        regionsSet = sharedPreferences.getStringSet(QuizActivity.REGIONS, null);
    }

    public void resetQuiz() {
        // Use AssetManager to get image file names for enabled regions
        AssetManager assets = getActivity().getAssets();
        fileNameList.clear();

        try {
            for (String region : regionsSet) {
                // Get a list of all flag image files in this region
                String[] paths = assets.list(region);

                for (String path : paths)
                    fileNameList.add(path.replace(".png", ""));
            }
        }
        catch (IOException e) {
            Log.e(TAG, "Error loading image file names", e);
        }
        // Reset correctAnswers, totalGuesses, and clear quizCountriesList
        correctAnswers = 0;
        totalGuesses = 0;
        quizCountriesList.clear();

        int flagCounter = 1;
        int numberOfFlags = fileNameList.size();

        while (flagCounter <= FLAGS_IN_QUIZ) {
            int randomIndex = random.nextInt(numberOfFlags);

            // Get random file name
            String filename = fileNameList.get(randomIndex);

            // If the region is enabled and hasn't already been chosen
            if (!quizCountriesList.contains(filename)) {
                quizCountriesList.add(filename);
                ++flagCounter;
            }
        }

        loadNextFlag();
    }

    private void loadNextFlag() {
        // Get file name of next flag and remove it from the list
        String nextImage = quizCountriesList.remove(0);
        correctAnswer = nextImage;
        answerTextView.setText("");

        // Display the current question number
        questionNumberTextView.setText(getString(R.string.question,
                (correctAnswers + 1), FLAGS_IN_QUIZ));

        // Extract the region from the next image's name
        String region = nextImage.substring(0, nextImage.indexOf('-'));

        // Use AssetManager to load next image from assets folder
        AssetManager assets = getActivity().getAssets();

        // Get an InputStream to the asset representing the next flag and try to use the InputStream
        try (InputStream stream = assets.open(region + "/" + nextImage + ".png")){

            // Load the assets as a drawable and display on flagImageView
            Drawable flag = Drawable.createFromStream(stream, nextImage);
            flagImageView.setImageDrawable(flag);
        }

        catch (IOException e) {
            Log.e(TAG, "Error loading " + nextImage, e);
        }

        // Shuffle file names
        Collections.shuffle(fileNameList);

        // Put the correct answers at the end of the file name list
        int correct = fileNameList.indexOf(correctAnswer);
        fileNameList.add(fileNameList.remove(correct));

        // Add 2, 4, 6, or 8 guess buttons based on the value of guessRows
        for (int row = 0; row < guessRows; ++row) {
            int columnCount = guessLinearLayouts[row].getChildCount();

            // Place buttons in currentTableRow
            for (int column = 0; column < columnCount; ++column) {
                // Get reference to button to configure
                Button newGuessButton = (Button) guessLinearLayouts[row].getChildAt(column);
                newGuessButton.setEnabled(true);

                // Get country name and set it as newGuessButton's text
                String filename = fileNameList.get((row * 2) + column);
                newGuessButton.setText(getCountryName(filename));
            }
        }
        // Randomly replace one button with the correct answer
        int row = random.nextInt(guessRows);
        int column = random.nextInt(2);
        LinearLayout randomRow = guessLinearLayouts[row];
        String countryName = getCountryName(correctAnswer);
        ((Button) randomRow.getChildAt(column)).setText(countryName);
    }

    private View.OnClickListener guessButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Button guessButton = ((Button) v);
            String guess = guessButton.getText().toString();
            String answer = getCountryName(correctAnswer);
            ++totalGuesses;

            if (guess.equals(answer)) {
                ++correctAnswers;

                // Display correct answer in green text
                answerTextView.setText(answer + "!");
                answerTextView.setTextColor(getResources().getColor(R.color.correct_answer,
                        getContext().getTheme()));

                // disable all guess buttons
                disableButtons();

                // If the user has correctly identified FLAGS_IN_QUIZ flags
                if (correctAnswers == FLAGS_IN_QUIZ) {
                    // DialogFragment to display quiz stats and start new quiz
                    DialogFragment quizResults = new DialogFragment() {
                        // Create an AlertDialog and return it
                        @Override
                        public Dialog onCreateDialog(Bundle bundle) {

                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setMessage(getString(R.string.results, totalGuesses,
                                    (1000.0 / (double) totalGuesses)));

                            // "Reset Quiz" button
                            builder.setPositiveButton(R.string.reset_quiz,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            resetQuiz();
                                        }
                                    });
                            // Return the AlertDialog
                            return builder.create();
                        }
                    };
                }
            }
        }
    };

    private String getCountryName(String s) {
        return s.replaceAll("_", " ");
    }

    private void disableButtons() {

    }
}
