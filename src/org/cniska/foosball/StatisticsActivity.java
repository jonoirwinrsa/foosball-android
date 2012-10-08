package org.cniska.foosball;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class StatisticsActivity extends Activity {

	// Enumerables
	// ----------------------------------------

	public enum SortColumn {
		PLAYER,
		GOALS,
		GOALS_AGAINST,
		WINS,
		LOSSES,
		GOALS_GOALS_AGAINST_RATIO,
		WIN_LOSS_RATIO
	}

	public enum SortDirection {
		ASCENDING,
		DESCENDING
	}

	// Static variables
	// ----------------------------------------

	private static final int LAYOUT_WEIGHT_PLAYER = 40;
	private static final int LAYOUT_WEIGHT_GOALS = 10;
	private static final int LAYOUT_WEIGHT_GOALS_AGAINST = 10;
	private static final int LAYOUT_WEIGHT_GOALS_GOALS_AGAINST_RATIO = 10;
	private static final int LAYOUT_WEIGHT_WINS = 10;
	private static final int LAYOUT_WEIGHT_LOSSES = 10;
	private static final int LAYOUT_WEIGHT_WIN_LOSS_RATIO = 10;

	// Member variables
	// ----------------------------------------

	private SQLitePlayerDataSource data;
	private List<Player> players;
	private PlayerComparator comparator;
	private TableLayout layout;

	// Methods
	// ----------------------------------------

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.statistics);

		data = new SQLitePlayerDataSource(this);
		data.open();

		players = data.findAllPlayers();
		comparator = new PlayerComparator();

		layout = (TableLayout) findViewById(R.id.table_statistics);
		addTableHeaderRow(layout);
		addTablePlayerRows(layout);

		sortByColumn(SortColumn.PLAYER);
	}

	@Override
	protected void onResume() {
		data.open();
		super.onResume();
	}

	@Override
	protected void onPause() {
		data.close();
		super.onPause();
	}

	/**
	 * Creates the table header row and adds it to the given table layout.
	 * @param layout The table layout.
	 */
	private void addTableHeaderRow(TableLayout layout) {
		TableRow row = new TableRow(this);

		TextView headerPlayer = createTableHeaderCell(getResources().getString(R.string.table_header_player), LAYOUT_WEIGHT_PLAYER, Gravity.LEFT, 10);
		headerPlayer.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				sortByColumn(SortColumn.PLAYER);
			}
		});
		row.addView(headerPlayer);

		TextView headerGoals = createTableHeaderCell(getResources().getString(R.string.table_header_goals), LAYOUT_WEIGHT_GOALS, Gravity.CENTER, 5);
		headerGoals.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				sortByColumn(SortColumn.GOALS);
			}
		});
		row.addView(headerGoals);

		TextView headerGoalsAgainst = createTableHeaderCell(getResources().getString(R.string.table_header_goals_against), LAYOUT_WEIGHT_GOALS_AGAINST, Gravity.CENTER, 10);
		headerGoalsAgainst.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				sortByColumn(SortColumn.GOALS_AGAINST);
			}
		});
		row.addView(headerGoalsAgainst);

		TextView headerWins = createTableHeaderCell(getResources().getString(R.string.table_header_wins), LAYOUT_WEIGHT_WINS, Gravity.CENTER, 10);
		headerWins.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				sortByColumn(SortColumn.WINS);
			}
		});
		row.addView(headerWins);

		TextView headerLosses = createTableHeaderCell(getResources().getString(R.string.table_header_losses), LAYOUT_WEIGHT_LOSSES, Gravity.CENTER, 10);
		headerLosses.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				sortByColumn(SortColumn.LOSSES);
			}
		});
		row.addView(headerLosses);

		TextView headerGoalsGoalsAgainstRatio = createTableHeaderCell(getResources().getString(R.string.table_header_goals_goals_against_ratio), LAYOUT_WEIGHT_GOALS_GOALS_AGAINST_RATIO, Gravity.CENTER, 10);
		headerGoalsGoalsAgainstRatio.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				sortByColumn(SortColumn.GOALS_GOALS_AGAINST_RATIO);
			}
		});
		row.addView(headerGoalsGoalsAgainstRatio);

		TextView headerWinLossRatio = createTableHeaderCell(getResources().getString(R.string.table_header_win_loss_ratio), LAYOUT_WEIGHT_WIN_LOSS_RATIO, Gravity.CENTER, 10);
		headerWinLossRatio.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				sortByColumn(SortColumn.WIN_LOSS_RATIO);
			}
		});
		row.addView(headerWinLossRatio);

		layout.addView(row);
	}

	/**
	 * Creates the player rows and adds them to the given table layout.
	 * @param layout The table layout.
	 */
	private void addTablePlayerRows(TableLayout layout) {
		int playerCount = players.size();

		if (playerCount > 0) {
			for (int i = 0; i < playerCount; i++) {
				Player player = players.get(i);
				TableRow row = new TableRow(this);
				row.addView(createTableCell(player.getName(), LAYOUT_WEIGHT_PLAYER, Gravity.LEFT, 10));
				row.addView(createTableCell(String.valueOf(player.getGoals()), LAYOUT_WEIGHT_GOALS, Gravity.CENTER, 10));
				row.addView(createTableCell(String.valueOf(player.getGoalsAgainst()), LAYOUT_WEIGHT_GOALS_AGAINST, Gravity.CENTER, 10));
				row.addView(createTableCell(String.valueOf(player.getWins()), LAYOUT_WEIGHT_WINS, Gravity.CENTER, 10));
				row.addView(createTableCell(String.valueOf(player.getLosses()), LAYOUT_WEIGHT_LOSSES, Gravity.CENTER, 10));
				row.addView(createTableCell(String.format("%2.01f", player.goalGoalAgainstRatio()), LAYOUT_WEIGHT_GOALS_GOALS_AGAINST_RATIO, Gravity.CENTER, 10));
				row.addView(createTableCell(String.format("%2.01f", player.winLossRatio()), LAYOUT_WEIGHT_WIN_LOSS_RATIO, Gravity.CENTER, 10));
				layout.addView(row);
			}
		}
	}

	/**
	 * Creates a single table header cell.
	 * @param text Cell text.
	 * @param weight Cell weight.
	 * @param gravity Cell gravity (think align).
	 * @param padding Cell padding.
	 * @return The cell.
	 */
	private TextView createTableHeaderCell(String text, float weight, int gravity, int padding) {
		TextView cell = createTableCell(text, weight, gravity, padding);
		cell.setAllCaps(true);
		cell.setTypeface(null, Typeface.BOLD);
		return cell;
	}

	/**
	 * Creates a single table cell.
	 * @param text Cell text.
	 * @param weight Cell weight.
	 * @param gravity Cell gravity (think align).
	 * @param padding Cell padding.
	 * @return The cell.
	 */
	private TextView createTableCell(String text, float weight, int gravity, int padding) {
		TextView cell = new TextView(this);
		cell.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, weight));
		cell.setPadding(padding, padding, padding, padding);
		cell.setGravity(gravity);
		cell.setText(text);
		return cell;
	}

	/**
	 * Sorts the table by the given column.
	 * @param column Sort column type.
	 */
	private void sortByColumn(SortColumn column) {
		layout.removeAllViews();
		addTableHeaderRow(layout);
		addTablePlayerRows(layout);
		comparator.sortColumn(column);
		Collections.sort(players, comparator);
		layout.invalidate();
	}

	private class PlayerComparator implements Comparator<Player> {

		private SortColumn column;
		private SortDirection direction;

		@Override
		public int compare(Player p1, Player p2) {
			if (column != null) {
				switch (column) {
					case GOALS:
						return compareInt(p1.getGoals(), p2.getGoals());
					case GOALS_AGAINST:
						return compareInt(p1.getGoalsAgainst(), p2.getGoalsAgainst());
					case WINS:
						return compareInt(p1.getWins(), p2.getWins());
					case LOSSES:
						return compareInt(p1.getLosses(), p2.getLosses());
					case GOALS_GOALS_AGAINST_RATIO:
						return compareFloat(p1.goalGoalAgainstRatio(), p2.goalGoalAgainstRatio());
					case WIN_LOSS_RATIO:
						return compareFloat(p1.winLossRatio(), p2.winLossRatio());
					case PLAYER:
					default:
						return compareString(p1.getName(), p2.getName());
				}
			} else {
				return 0;
			}
		}

		/**
		 * Compares two strings and returns the result.
		 * @param value1 String 1.
		 * @param value2 String 2.
		 * @return The result.
		 */
		private int compareString(String value1, String value2) {
			if (direction == SortDirection.ASCENDING) {
				return value1.compareTo(value2);
			} else {
				return value2.compareTo(value1);
			}
		}

		/**
		 * Compares two integers and returns the result.
		 * @param value1 Integer 1.
		 * @param value2 Integer 2.
		 * @return The result.
		 */
		private int compareInt(int value1, int value2) {
			return compareFloat(value1, value2);
		}

		/**
		 * Compares two floats and returns the result.
		 * @param value1 Float 1.
		 * @param value2 Float 2.
		 * @return The result.
		 */
		private int compareFloat(float value1, float value2) {
			if (direction == SortDirection.ASCENDING && (value1 < value2)
					|| direction == SortDirection.DESCENDING && (value2 < value1)) {
				return 1;
			} else if (direction == SortDirection.ASCENDING && (value1 > value2)
					|| direction == SortDirection.DESCENDING && (value2 > value1)) {
				return -1;
			} else {
				return 0;
			}
		}

		/**
		 * Changes the sorting order for the comparator.
		 * @param column Sort column type.
		 */
		public void sortColumn(SortColumn column) {
			if (direction != null && column == this.column) {
				direction = oppositeDirection();
			} else {
				direction = defaultDirection();
			}

			this.column = column;
		}

		/**
		 * Returns the opposite sorting direction to the current.
		 * @return The direction.
		 */
		private SortDirection oppositeDirection() {
			return direction == SortDirection.ASCENDING ? SortDirection.DESCENDING : SortDirection.ASCENDING;
		}

		/**
		 * Returns the default sorting direction.
		 * @return The direction.
		 */
		public SortDirection defaultDirection() {
			return SortDirection.ASCENDING;
		}
	}
}