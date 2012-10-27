package org.cniska.foosball.android;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.*;

import java.util.*;

/**
 * This activity handles match creation.
 */
public class NewMatchActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<Cursor> {

	// Static variables
	// ----------------------------------------

	public static final String TAG = "NewMatchActivity";

	public static final String EXTRA_MATCH = "org.cniska.foosball.android.EXTRA_MATCH";

	private static final int AUTO_COMPLETE_THRESHOLD = 1;

	private static final int EDIT_TEXT_PLAYER_1 = 0;
	private static final int EDIT_TEXT_PLAYER_2 = 1;
	private static final int EDIT_TEXT_PLAYER_3 = 2;
	private static final int EDIT_TEXT_PLAYER_4 = 3;

	private static String[] PLAYER_PROJECTION = {
		DataContract.Players._ID,
		DataContract.Players.NAME
	};

	private static int[] sEditTextIds = new int[] {
		R.id.edit_text_player1,
		R.id.edit_text_player2,
		R.id.edit_text_player3,
		R.id.edit_text_player4
	};

	// Member variables
	// ----------------------------------------

	private AutoCompleteTextView[] mEditTexts = new AutoCompleteTextView[RawMatch.NUM_SUPPORTED_PLAYERS];
	private Map<String, Long> mNameIdMap = new HashMap<String, Long>(RawMatch.NUM_SUPPORTED_PLAYERS);

	// Methods
	// ----------------------------------------

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHomeButtonEnabled(true);
		getActionBar().setTitle(getString(R.string.title_new_match));
		setContentView(R.layout.new_match);

		// Collect the auto-complete views so that we can refer to them later.
		for (int i = 0; i < RawMatch.NUM_SUPPORTED_PLAYERS; i++) {
			mEditTexts[i] = (AutoCompleteTextView) findViewById(sEditTextIds[i]);
		}

		getSupportLoaderManager().initLoader(0, null, this);
		Logger.info(TAG, "Activity created.");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.new_match, menu);
		return true;
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(getApplicationContext(), DataContract.Players.CONTENT_URI, PLAYER_PROJECTION,
				null, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		if (data.moveToNext()) {
			// Collect the player names for the auto-completion.
			ArrayList<String> playerNames = new ArrayList<String>(data.getCount());

			while (!data.isAfterLast()) {
                long id = data.getLong(data.getColumnIndex(DataContract.Players._ID));
				String name = data.getString(data.getColumnIndex(DataContract.Players.NAME));
				playerNames.add(name);
				mNameIdMap.put(name, id);
				data.moveToNext();
			}

			// Create the auto-complete adapter.
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(
					this, android.R.layout.simple_spinner_dropdown_item, playerNames);

			// Bind auto-completion for the edit text views.
			for (int i = 0; i < RawMatch.NUM_SUPPORTED_PLAYERS; i++) {
				mEditTexts[i].setThreshold(AUTO_COMPLETE_THRESHOLD);
				mEditTexts[i].setAdapter(adapter);
			}
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
	}

	/**
	 * Submits the form.
	 * @param view
	 */
	public void submitForm(View view) {
		// Resolve the radio value.
		RadioGroup scoresToWin = (RadioGroup) findViewById(R.id.radio_group_score_to_win);
		int checkedRadioId = scoresToWin.getCheckedRadioButtonId();
		RadioButton checkedRadio = (RadioButton) findViewById(checkedRadioId);

		// Make sure that there weren't any validation errors before continuing.
		if (validateForm()) {
            String[] data = new String[RawMatch.NUM_SUPPORTED_PLAYERS];
			ArrayList<Long> idList = new ArrayList<Long>();

            for (int i = 0; i < RawMatch.NUM_SUPPORTED_PLAYERS; i++) {
				String name = mEditTexts[i].getText().toString().trim();

				data[i] = name;

				// Makes sure that the edit text is filled out before doing anything with it.
				if (!TextUtils.isEmpty(name)) {
					if (!mNameIdMap.containsKey(name)) {
						// Each player name that isn't in the map is a new player and needs to be created.
						ContentValues values = new ContentValues();
						values.put(DataContract.Players.NAME, name);
						RawPlayer player = createPlayer(values);
						mNameIdMap.put(name, player.getId());

						ContentValues ratingValues = new ContentValues();
						ratingValues.put(DataContract.Ratings.PLAYER_ID, player.getId());
						ratingValues.put(DataContract.Ratings.RATING, EloRatingSystem.INITIAL_RATING);
						getContentResolver().insert(DataContract.Ratings.CONTENT_URI, ratingValues);
					}

					idList.add(mNameIdMap.get(name));
				}
            }

			// Randomize teams if necessary. This can only be done if all four players are playing.
			if (!TextUtils.isEmpty(data[EDIT_TEXT_PLAYER_3]) && !TextUtils.isEmpty(data[EDIT_TEXT_PLAYER_4])) {
				CheckBox randomTeams = (CheckBox) findViewById(R.id.check_box_random_teams);
				if (randomTeams.isChecked()) {
					Random random = new Random(System.currentTimeMillis());
					Collections.shuffle(idList, random);
				}
			}

            int numGoalsToWin = Integer.parseInt(checkedRadio.getText().toString());

			long[] playerIds = new long[RawMatch.NUM_SUPPORTED_PLAYERS];
			for (int i = 0, len = idList.size(); i < len; i++) {
				playerIds[i] = idList.get(i);
			}

            RawMatch match = new RawMatch();
            match.setNumGoalsToWin(numGoalsToWin);
			match.setPlayerIds(playerIds);

			Logger.info(TAG, "Sending intent to start PlayMatchActivity.");
			Intent intent = new Intent(this, PlayMatchActivity.class);
			intent.putExtra(EXTRA_MATCH, match);
			startActivity(intent);
		}
	}

	/**
	 * Creates a new player with the given values.
	 * @param values Content values.
	 * @return The player.
	 */
	private RawPlayer createPlayer(ContentValues values) {
		RawPlayer player = null;

		ContentResolver contentResolver = getContentResolver();
		Uri uri = contentResolver.insert(DataContract.Players.CONTENT_URI, values);
		Cursor cursor = contentResolver.query(uri, PLAYER_PROJECTION, null, null, null);

		if (cursor.moveToFirst()) {
			player = new RawPlayer(cursor);
		}

		return player;
	}

	/**
	 * Validates the form.
	 * @return Whether the form is valid.
	 */
	private boolean validateForm() {
		// Make sure that player 1 is given.
		if (mEditTexts[0].getText().toString().isEmpty()) {
			mEditTexts[0].setError("Player 1 is required");
			return false;
		}

		// Make sure that player 2 is given.
		if (mEditTexts[1].getText().toString().isEmpty()) {
			mEditTexts[1].setError("Player 2 is required");
			return false;
		}

		return true;
	}
}