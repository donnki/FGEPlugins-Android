package com.luciolagames.libfgeplugins;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.cocos2dx.lib.Cocos2dxActivity;
import org.cocos2dx.lib.Cocos2dxLuaJavaBridge;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.inputmethod.InputMethodSession.EventCallback;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.Player;
import com.google.android.gms.games.event.Events;
import com.google.android.gms.games.event.Events.LoadEventsResult;
import com.google.android.gms.games.leaderboard.LeaderboardScore;
import com.google.android.gms.games.leaderboard.LeaderboardScoreBuffer;
import com.google.android.gms.games.leaderboard.LeaderboardVariant;
import com.google.android.gms.games.leaderboard.Leaderboards;
import com.google.android.gms.games.leaderboard.Leaderboards.LoadScoresResult;
import com.google.android.gms.games.quest.Quest;
import com.google.android.gms.games.quest.QuestBuffer;
import com.google.android.gms.games.quest.QuestUpdateListener;
import com.google.android.gms.games.quest.Quests;
import com.google.android.gms.games.quest.Quests.LoadQuestsResult;
import com.google.android.gms.games.snapshot.Snapshot;
import com.google.android.gms.games.snapshot.SnapshotMetadata;
import com.google.android.gms.games.snapshot.SnapshotMetadataChange;
import com.google.android.gms.games.snapshot.Snapshots;
import com.google.android.gms.plus.Plus;
import com.google.example.games.basegameutils.BaseGameUtils;
import com.luciolagames.libfgeplugins.R;

public class GooglePlayGameServicePlugin implements
		GoogleApiClient.ConnectionCallbacks,
		GoogleApiClient.OnConnectionFailedListener, QuestUpdateListener {
	private static final String TAG = "GAME";
	private static final int PAGE_MAX = 10;
	private static final int APP_STATE_KEY = 0;
	
	private static final int RC_RESOLVE = 5000;
	private static final int RC_UNUSED = 5001;
	private static final int RC_SIGN_IN = 9001;
	private static final int RC_SELECT_SNAPSHOT = 9002;
	private static final int RC_SAVED_GAMES = 9009;
	

	private static GooglePlayGameServicePlugin sInstance;
    
	private GoogleApiClient mGoogleApiClient;
	private Cocos2dxActivity context;
	private ResultCallback<Quests.ClaimMilestoneResult> mClaimMilestoneResultCallback;
	
	private boolean mResolvingConnectionFailure = false;
	private boolean mSignInClicked = false;
	private boolean mAutoStartSignInFlow = true;
	
	private String mCurrentSaveName;

	public GooglePlayGameServicePlugin(Cocos2dxActivity activity) {
		this.context = activity;
		sInstance = this;
		initGameService();
	}

	private void initGameService() {
		mGoogleApiClient = new GoogleApiClient.Builder(context)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this).addApi(Plus.API)
				.addScope(Plus.SCOPE_PLUS_LOGIN).addApi(Games.API)
				.addScope(Games.SCOPE_GAMES)
				.build();
		//.addApi(Drive.API).addScope(Drive.SCOPE_APPFOLDER)
		// Set the callback for when milestones are claimed.
        mClaimMilestoneResultCallback = new ResultCallback<Quests.ClaimMilestoneResult>() {
            @Override
            public void onResult(Quests.ClaimMilestoneResult result) {
            	try {
                    if (result.getStatus().isSuccess()){
                        String reward = new String(result.getQuest().getCurrentMilestone().
                                getCompletionRewardData(),
                                "UTF-8");
                        // TOAST to let the player what they were rewarded.
                        Toast.makeText(context, "Congratulations, you got a " + reward,
                                Toast.LENGTH_LONG).show();
                    } else {
                        Log.e(TAG, "Reward was not claimed due to error.");
                        Toast.makeText(context, "Reward was not claimed due to error.",
                                Toast.LENGTH_LONG).show();
                    }
                } catch (UnsupportedEncodingException uee) {
                    uee.printStackTrace();
                }
            }
        };
	}

	public void onStart() {
		if (mGoogleApiClient != null) {
			Log.d(TAG, "onStart(): connecting");
			mGoogleApiClient.connect();
		}
	}

	public void onStop() {
		if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
			Log.d(TAG, "onStop(): disconnecting");
			mGoogleApiClient.disconnect();
		}
	}

	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (requestCode == RC_SIGN_IN) {
			mResolvingConnectionFailure = false;
			if (resultCode == Activity.RESULT_OK) {
				mGoogleApiClient.connect();
			} else {
				BaseGameUtils.showActivityResultError(context, requestCode,
						resultCode, R.string.signin_other_error);
			}
		} else if (requestCode == RC_SELECT_SNAPSHOT) {
            Log.d(TAG, "onActivityResult: RC_SELECT_SNAPSHOT, resultCode = " + resultCode);
            if (resultCode == Activity.RESULT_OK) {
                // Successfully returned from Snapshot selection UI
                if (intent != null) {
                    Bundle bundle = intent.getExtras();
                    SnapshotMetadata selected = Games.Snapshots.getSnapshotFromBundle(bundle);
                    if (selected == null) {
                        // No snapshot in the Intent bundle, display error message
                        // displayMessage(getString(R.string.saved_games_select_failure), true);
                        // setData(null);
                        // displaySnapshotMetadata(null);
                    } else {
                        // Found Snapshot Metadata in Intent bundle.  Load Snapshot by name.
                        String snapshotName = selected.getUniqueName();
                        //savedGamesLoad(snapshotName);
                    }
                }
            } else {
                // User canceled the select intent or it failed for some other reason
                //displayMessage(getString(R.string.saved_games_select_cancel), true);
                //setData(null);
                //displaySnapshotMetadata(null);
            }
        }
		
	}

	private boolean isSignedIn() {
		return (mGoogleApiClient != null && mGoogleApiClient.isConnected());
	}

	
	@Override
	public void onConnected(Bundle bundle) {
		Log.d(TAG, "onConnected(): connected to Google APIs");

		Player p = Games.Players.getCurrentPlayer(mGoogleApiClient);
		String displayName;
		if (p == null) {
			Log.w(TAG, "mGamesClient.getCurrentPlayer() is NULL!");
			displayName = "???";
		} else {
			displayName = p.getDisplayName();
		}
		// TODO: say hello
		Log.d(TAG, "Hello, " + displayName);
		
		Games.Quests.registerQuestUpdateListener(mGoogleApiClient, this);
	}
	
	

	@Override
	public void onConnectionSuspended(int i) {
		Log.d(TAG, "onConnectionSuspended(): attempting to connect");
		mGoogleApiClient.connect();
	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		Log.d(TAG, "onConnectionFailed(): attempting to resolve");
		if (mResolvingConnectionFailure) {
			Log.d(TAG, "onConnectionFailed(): already resolving");
			return;
		}
		// TODO: Sign-in failed, so show sign-in button on main menu
		if (mSignInClicked || mAutoStartSignInFlow) {
			mAutoStartSignInFlow = false;
			mSignInClicked = false;
			mResolvingConnectionFailure = true;
			if (!BaseGameUtils.resolveConnectionFailure(context,
					mGoogleApiClient, connectionResult, RC_SIGN_IN,
					context.getString(R.string.signin_other_error))) {
				mResolvingConnectionFailure = false;
			}
		}
	}
	
	/**
     * Load a Snapshot from the Saved Games service based on its unique name.  After load, the UI
     * will update to display the Snapshot data and SnapshotMetadata.
     * @param snapshotName the unique name of the Snapshot.
     */
    private void savedGamesLoad(String snapshotName) {
        PendingResult<Snapshots.OpenSnapshotResult> pendingResult = Games.Snapshots.open(
                mGoogleApiClient, snapshotName, false);

        Log.d(TAG, "Loading Saved Game");
        ResultCallback<Snapshots.OpenSnapshotResult> callback =
                new ResultCallback<Snapshots.OpenSnapshotResult>() {
            @Override
            public void onResult(Snapshots.OpenSnapshotResult openSnapshotResult) {
                if (openSnapshotResult.getStatus().isSuccess()) {
                	Log.i(TAG, "saved_games_load_success");
                    byte[] data = new byte[0];
                    try {
                        data = openSnapshotResult.getSnapshot().getSnapshotContents().readFully();
                    } catch (IOException e) {
                        Log.e(TAG, "Exception reading snapshot: " + e.getMessage());
                    }
                    //setData(new String(data));
                    //displaySnapshotMetadata(openSnapshotResult.getSnapshot().getMetadata());
                } else {
                	Log.w(TAG, "saved_games_load_failure");
                    //clearDataUI();
                }

                //dismissProgressDialog();
            }
        };
        pendingResult.setResultCallback(callback);
    }
    
    /**
     * Launch the UI to select a Snapshot from the user's Saved Games.  The result of this
     * selection will be returned to onActivityResult.
     */
    private void savedGamesSelect() {
        final boolean allowAddButton = false;
        final boolean allowDelete = false;
        Intent intent = Games.Snapshots.getSelectSnapshotIntent(
                mGoogleApiClient, "Saved Games", allowAddButton, allowDelete,
                Snapshots.DISPLAY_LIMIT_NONE);

//        showProgressDialog("Loading");
        context.startActivityForResult(intent, RC_SELECT_SNAPSHOT);
    }
    
    /**
     * Generate a unique Snapshot name from an AppState stateKey.
     * @param appStateKey the stateKey for the Cloud Save data.
     * @return a unique Snapshot name that maps to the stateKey.
     */
    private String makeSnapshotName(int appStateKey) {
        return "Snapshot-" + String.valueOf(appStateKey);
    }
    
    private String savedData;
    /**
     * Get the data from the EditText.
     * @return the String in the EditText, or "" if empty.
     */
    private String getData() {
        return savedData;
    }
    
    /**
     * Update the Snapshot in the Saved Games service with new data.  Metadata is not affected,
     * however for your own application you will likely want to update metadata such as cover image,
     * played time, and description with each Snapshot update.  After update, the UI will
     * be cleared.
     */
    private void savedGamesUpdate() {
        final String snapshotName = makeSnapshotName(APP_STATE_KEY);
        final boolean createIfMissing = false;

        // Use the data from the EditText as the new Snapshot data.
        final byte[] data = getData().getBytes();

        AsyncTask<Void, Void, Boolean> updateTask = new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected void onPreExecute() {
                Log.i(TAG, "Updating Saved Game");
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                Snapshots.OpenSnapshotResult open = Games.Snapshots.open(
                        mGoogleApiClient, snapshotName, createIfMissing).await();

                if (!open.getStatus().isSuccess()) {
                    Log.w(TAG, "Could not open Snapshot for update.");
                    return false;
                }

                // Change data but leave existing metadata
                Snapshot snapshot = open.getSnapshot();
                snapshot.getSnapshotContents().writeBytes(data);

                Snapshots.CommitSnapshotResult commit = Games.Snapshots.commitAndClose(
                        mGoogleApiClient, snapshot, SnapshotMetadataChange.EMPTY_CHANGE).await();

                if (!commit.getStatus().isSuccess()) {
                    Log.w(TAG, "Failed to commit Snapshot.");
                    return false;
                }

                // No failures
                return true;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                if (result) {
                    Log.i(TAG, "saved_games_update_success");
                } else {
                	Log.i(TAG, "saved_games_update_failure");
                }

                //dismissProgressDialog();
                //clearDataUI();
            }
        };
        updateTask.execute();
    }
    
	/**
     * Event handler for when Quests are completed.
     *
     * @param quest The quest that has been completed.
     */
    @Override
    public void onQuestCompleted(Quest quest) {
        // create a message string indicating that the quest was successfully completed
        String message = "You successfully completed quest " + quest.getName();

        // Print out message for debugging purposes.
        Log.i(TAG, message);

        // Create a custom toast to indicate the quest was successfully completed.
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();

        // Claim the quest reward.
        Games.Quests.claim(
                mGoogleApiClient,
                quest.getQuestId(),
                quest.getCurrentMilestone().getMilestoneId())
                .setResultCallback(mClaimMilestoneResultCallback);
    }
    
    /**
     * Shows the current list of available quests.
     */
    public void onShowQuests() {
        int[] selection = {Quests.SELECT_OPEN, Quests.SELECT_COMPLETED_UNCLAIMED,
                Quests.SELECT_ACCEPTED};
        android.content.Intent questsIntent = Games.Quests.getQuestsIntent(mGoogleApiClient,
                selection);
        context.startActivityForResult(questsIntent, 0);
    }
    
    public void loadAndListQuests() {
        int[] selection = {Quests.SELECT_OPEN, Quests.SELECT_COMPLETED_UNCLAIMED,
                Quests.SELECT_ACCEPTED};
        com.google.android.gms.common.api.PendingResult<Quests.LoadQuestsResult> pr =
                Games.Quests.load(mGoogleApiClient, selection,
                Quests.SORT_ORDER_ENDING_SOON_FIRST, true);

        // Set the callback to the Quest callback.
        pr.setResultCallback(new ResultCallback<Quests.LoadQuestsResult>() {
			@Override
			public void onResult(LoadQuestsResult result) {
				QuestBuffer qb = result.getQuests();

	            String message = "Current quest details: \n";

	            Log.i(TAG, "Number of quests: " + qb.getCount());

	            for(int i=0; i < qb.getCount(); i++) {
	                message += "Quest: " + qb.get(i).getName() + " id: " + qb.get(i).getQuestId();
	            }
	            qb.close();
	            Log.i(TAG, message);
			}
		});
    }
    
    public void loadAndPrintEvents() {
        // Load up a list of events
        com.google.android.gms.common.api.PendingResult<Events.LoadEventsResult> pr =
                Games.Events.load(mGoogleApiClient, true);

        // Set the callback to the EventCallback class.
        pr.setResultCallback(new ResultCallback<Events.LoadEventsResult>() {
			@Override
			public void onResult(LoadEventsResult result) {
				com.google.android.gms.games.event.EventBuffer eb = result.getEvents();

	            String message = "Current stats: \n";

	            Log.i(TAG, "number of events: " + eb.getCount());

	            for(int i=0; i < eb.getCount(); i++) {
	                message += "event: " + eb.get(i).getName() + " " + eb.get(i).getEventId() +
	                        " " + eb.get(i).getValue() + "\n";
	            }
	            eb.close(); 
	            Log.i(TAG, message);
	            
			}
			
		});
    }

	public void onShowAchievementsRequested() {
		if (isSignedIn()) {
			context.startActivityForResult(
					Games.Achievements.getAchievementsIntent(mGoogleApiClient),
					RC_UNUSED);
			
		} else {
			BaseGameUtils.makeSimpleDialog(context,
					context.getString(R.string.achievements_not_available))
					.show();
		}
	}

	public void onLoadLeaderboardData(String id, int span,
			int leaderboardCollection, final int callback) {
		if (isSignedIn()) {
			PendingResult<Leaderboards.LoadScoresResult> result = Games.Leaderboards
					.loadPlayerCenteredScores(mGoogleApiClient, id, span,
							leaderboardCollection, PAGE_MAX);
			result.setResultCallback(new ResultCallback<LoadScoresResult>() {
				@Override
				public void onResult(LoadScoresResult result) {

					if (result.getStatus().getStatusCode() == GamesStatusCodes.STATUS_OK) {
						String leaderboardId = result.getLeaderboard()
								.getLeaderboardId();
						String leaderboardName = result.getLeaderboard()
								.getDisplayName();
						LeaderboardScoreBuffer scores = result.getScores();
						
						List<Map<String, String>> lscores = new ArrayList<Map<String, String>>();
						int size = scores.getCount();
						for (int i = 0; i < size; i++) {
							LeaderboardScore l = scores.get(i);
							Map<String, String> map = new HashMap<String, String>();
							map.put("rank", l.getDisplayRank());
							map.put("formatScore", l.getDisplayScore());
							map.put("score", l.getRawScore() + "");
							map.put("name", l.getScoreHolderDisplayName());
							map.put("playerId", l.getScoreHolder()
									.getPlayerId());
							lscores.add(map);
						}
						JSONArray obj = new JSONArray(lscores);
						final String response = obj.toString();
						Log.d(TAG, "response: " + response);
						context.runOnGLThread(new Runnable() {
				            @Override
				            public void run() {
				            	Cocos2dxLuaJavaBridge.callLuaFunctionWithString(callback, response);
				            	Cocos2dxLuaJavaBridge.releaseLuaFunction(callback);
				            }
				          });
						
						scores.close();
					}

				}
			});
		} else {
			BaseGameUtils.makeSimpleDialog(context,
					context.getString(R.string.achievements_not_available))
					.show();
		}
	}

	public void onShowLeaderboardsRequested() {
		if (isSignedIn()) {
			context.startActivityForResult(Games.Leaderboards
					.getAllLeaderboardsIntent(mGoogleApiClient), RC_UNUSED);
		} else {
			BaseGameUtils.makeSimpleDialog(context,
					context.getString(R.string.leaderboards_not_available))
					.show();
		}
	}

	public void onShowLeaderboardRequested(String leaderboardID, int timeSpan) {
		if (isSignedIn()) {

			context.startActivityForResult(Games.Leaderboards
					.getLeaderboardIntent(mGoogleApiClient, leaderboardID,
							timeSpan), RC_UNUSED);
		} else {
			BaseGameUtils.makeSimpleDialog(context,
					context.getString(R.string.leaderboards_not_available))
					.show();
		}
	}

	public void onLeaderboardSubmitScore(String leaderboardID, long score) {
		if (isSignedIn()) {
			Log.d(TAG, "onLeaderboardSubmitScore: " + score);
			Games.Leaderboards.submitScoreImmediate(mGoogleApiClient,
					leaderboardID, score);
		} else {
			BaseGameUtils.makeSimpleDialog(context,
					context.getString(R.string.leaderboards_not_available))
					.show();
		}
	}

	public void unlockAchievementRequest(String achievementId,
			String fallbackString) {
		if (isSignedIn()) {
			Games.Achievements.unlock(mGoogleApiClient, achievementId);
		} else {
			Toast.makeText(
					context,
					context.getString(R.string.achievement) + ": "
							+ fallbackString, Toast.LENGTH_LONG).show();
		}
	}
	
	public void showSavedGamesUI() {
	    int maxNumberOfSavedGamesToShow = 5;
	    Intent savedGamesIntent = Games.Snapshots.getSelectSnapshotIntent(mGoogleApiClient,
	            "See My Saves", true, true, maxNumberOfSavedGamesToShow);
	    context.startActivityForResult(savedGamesIntent, RC_SAVED_GAMES);
	}

	// 以下是对LUA的静态接口
	/**
	 * 显示排行榜列表
	 * */
	public static void showLeaderboards() {
		Log.d(TAG, "showLeaderboards");
		sInstance.onShowLeaderboardsRequested();
	}

	/**
	 * 显示成就列表
	 * */
	public static void showAchievements() {
		Log.d(TAG, "showAchievements");
		sInstance.onShowAchievementsRequested();
	}

	/**
	 * 解锁成就
	 * */
	public static void unlockAchievement(String achievementID) {
		Log.d(TAG, "unlockAchievement: " + achievementID);
		sInstance.unlockAchievementRequest(achievementID, "");
	}

	/**
	 * 显示指定排行榜ID的总排行榜
	 * */
	public static void showLeaderboardByID(String id, int span) {
		Log.d(TAG, "showLeaderboards");
		int timeSpan = LeaderboardVariant.TIME_SPAN_ALL_TIME;
		if (span == 1) {
			timeSpan = LeaderboardVariant.TIME_SPAN_DAILY;
		} else if (span == 2) {
			timeSpan = LeaderboardVariant.TIME_SPAN_WEEKLY;
		}
		sInstance.onShowLeaderboardRequested(id, timeSpan);
	}

	/**
	 * 提交指定排行榜分数
	 * */
	public static void submitLeaderboardScore(String id, int score) {
		Log.d(TAG, "submitLeaderboardScore," + id + ", " + score);
		sInstance.onLeaderboardSubmitScore(id, score);
	}
	
	/**
	 * 获取指定排行榜分数
	 * 在callback中返回json排行榜数据
	 * */
	public static void loadLeaderboardScore(String id, int callback){
		int span = LeaderboardVariant.TIME_SPAN_ALL_TIME;
		int leaderboardCollection = LeaderboardVariant.COLLECTION_PUBLIC;
		sInstance.onLoadLeaderboardData(id, span, leaderboardCollection, callback);
	}
	
	/**
	 * 显示任务列表
	 * */
	public static void showQuests(){
		sInstance.loadAndListQuests(); 			
		sInstance.loadAndPrintEvents();
		sInstance.onShowQuests();
	}
	
}
