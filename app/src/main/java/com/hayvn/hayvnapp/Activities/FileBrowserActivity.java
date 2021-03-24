package com.hayvn.hayvnapp.Activities;

//https://github.com/mburman/Android-File-Explore

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Collections;
import java.util.Objects;

//Android imports 
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.*;
import android.widget.*;

//Import of resources file for file browser
import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;

import com.hayvn.hayvnapp.Constant.TextConstants;
import com.hayvn.hayvnapp.R;
import com.hayvn.hayvnapp.databinding.ActivityFilepickerBinding;

import org.jetbrains.annotations.NotNull;

public class FileBrowserActivity extends BaseParentActivity {
	public static final String INTENT_ACTION_SELECT_FILE = "filebrowser.SELECT_FILE_ACTION";
	public static final String startDirectoryParameter = "filebrowser.directoryPath";
	public static final String returnDirectoryParameter = "filebrowser.directoryPathRet";
	public static final String returnFileParameter = "filebrowser.filePathRet";
	public static final String showCannotReadParameter = "filebrowser.showCannotRead";
	public static final String filterExtension = "filebrowser.filterExtension";

	ArrayList<String> pathDirsList = new ArrayList<String>();
	ArrayAdapter<Item> adapter;

	private static final String TAG = "F_PATH";
	private List<Item> fileList = new ArrayList<Item>();
	private File path = null;
	private String chosenFile;
	private boolean showHiddenFilesAndDirs = true;
	private boolean directoryShownIsEmpty = false;
	private String filterFileExtension = null;
	private static int currentAction = -1;
	private static final int SELECT_DIRECTORY = 1;
	private static final int SELECT_FILE = 2;

	private ActivityFilepickerBinding binding;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// In case of
		// ua.com.vassiliev.androidfilebrowser.SELECT_DIRECTORY_ACTION
		// Expects com.mburman.fileexplore.directoryPath parameter to
		// point to the start folder.
		// If empty or null, will start from SDcard root.
		binding = ActivityFilepickerBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		Intent thisInt = this.getIntent();
		currentAction = SELECT_DIRECTORY;
		if (Objects.requireNonNull(thisInt.getAction()).equalsIgnoreCase(INTENT_ACTION_SELECT_FILE)) {
			currentAction = SELECT_FILE;
		}
		showHiddenFilesAndDirs = thisInt.getBooleanExtra(
				showCannotReadParameter, true);
		filterFileExtension = thisInt.getStringExtra(filterExtension);
		setInitialDirectory();
		parseDirectoryPath();
		loadFileList();
		this.createFileListAdapter();
		this.initializeButtons();
		this.initializeFileListView();
		updateCurrentDirectoryTextView();
		Log.d(TAG, path.getAbsolutePath());
	}

	private void setInitialDirectory() {
		Intent thisInt = this.getIntent();
		String requestedStartDir = thisInt
				.getStringExtra(startDirectoryParameter);

		if (requestedStartDir != null && requestedStartDir.length() > 0) {// if(requestedStartDir!=null
			File tempFile = new File(requestedStartDir);
			if (tempFile.isDirectory())
				this.path = tempFile;
		}

		if (this.path == null) {
			if (Environment.getExternalStorageDirectory().isDirectory()
					&& Environment.getExternalStorageDirectory().canRead())
				path = Environment.getExternalStorageDirectory();
			else
				path = new File("/");
		}
	}

	private void parseDirectoryPath() {
		pathDirsList.clear();
		String pathString = path.getAbsolutePath();
		String[] parts = pathString.split("/");
		int i = 0;
		while (i < parts.length) {
			pathDirsList.add(parts[i]);
			i++;
		}
	}

	private void initializeButtons() {

		binding.upDirectoryButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Log.d(TAG, "onclick for upDirButton");
				loadDirectoryUp();
				loadFileList();
				adapter.notifyDataSetChanged();
				updateCurrentDirectoryTextView();
			}
		});// upDirButton.setOnClickListener(


		if (currentAction == SELECT_DIRECTORY) {
			binding.selectCurrentDirectoryButton.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					Log.d(TAG, "onclick for selectFolderButton");
					returnDirectoryFinishActivity();
				}
			});

		} else {// if(currentAction == this.SELECT_DIRECTORY) {
			binding.selectCurrentDirectoryButton.setVisibility(View.GONE);
		}// } else {//if(currentAction == this.SELECT_DIRECTORY) {
	}// private void initializeButtons() {


	private void loadDirectoryUp() {
		String s = pathDirsList.remove(pathDirsList.size() - 1);
		path = new File(path.toString().substring(0,
				path.toString().lastIndexOf(s)));
		fileList.clear();
	}

	private void updateCurrentDirectoryTextView() {
		int i = 0;
		StringBuilder curDirString = new StringBuilder();
		while (i < pathDirsList.size()) {
			curDirString.append(pathDirsList.get(i)).append("/");
			i++;
		}
		if (pathDirsList.size() == 0) {
			binding.upDirectoryButton.setEnabled(false);
			curDirString = new StringBuilder("/");
		} else
			binding.upDirectoryButton.setEnabled(true);
		long freeSpace = getFreeSpace(curDirString.toString());
		String formattedSpaceString = formatBytes(freeSpace);
		if (freeSpace == 0) {
			Log.d(TAG, "NO FREE SPACE");
			File currentDir = new File(curDirString.toString());
			if(!currentDir.canWrite())
				formattedSpaceString = TextConstants.NON_WRITABLE;
		}

		String to_display1 = TextConstants.SELECT +  "\n[" + formattedSpaceString + "]";
		String to_display2 = TextConstants.CURRENT_DIRECTORY + ": " + curDirString;
		binding.selectCurrentDirectoryButton.setText(to_display1);
		binding.currentDirectoryTextView.setText(to_display2);
	}// END private void updateCurrentDirectoryTextView() {


	private void showToast() {
		Toast.makeText(this, TextConstants.PATH_NON_EXIST, Toast.LENGTH_LONG).show();
	}

	private void initializeFileListView() {

		binding.fileListView.setBackgroundColor(Color.TRANSPARENT);
		LinearLayout.LayoutParams lParam = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		lParam.setMargins(15, 5, 15, 5);
		binding.fileListView.setAdapter(this.adapter);
		binding.fileListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				chosenFile = fileList.get(position).file;
				File sel = new File(path + "/" + chosenFile);
				Log.d(TAG, "Clicked:" + chosenFile);
				if (sel.isDirectory()) {
					if (sel.canRead()) {
						// Adds chosen directory to list
						pathDirsList.add(chosenFile);
						path = new File(sel + "");
						Log.d(TAG, "Just reloading the list");
						loadFileList();
						adapter.notifyDataSetChanged();
						updateCurrentDirectoryTextView();
						Log.d(TAG, path.getAbsolutePath());
					} else {// if(sel.canRead()) {
						showToast();
					}
				}
				else {
					Log.d(TAG, "item clicked");
					if (!directoryShownIsEmpty) {
						Log.d(TAG, "File selected:" + chosenFile);
						returnFileFinishActivity(sel.getAbsolutePath());
					}
				}
			}
		});
	}

	private void returnDirectoryFinishActivity() {
		Intent retIntent = new Intent();
		retIntent.putExtra(returnDirectoryParameter, path.getAbsolutePath());
		this.setResult(RESULT_OK, retIntent);
		this.finish();
	}

	private void returnFileFinishActivity(String filePath) {
		Intent retIntent = new Intent();
		File newFile = new File(filePath);
		Uri contentUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName(), newFile);
		retIntent.putExtra(returnFileParameter, contentUri.toString());
		this.setResult(RESULT_OK, retIntent);
		this.finish();
	}

	private void loadFileList() {
		try {
			boolean r = path.mkdirs();
		} catch (SecurityException e) {
			Log.e(TAG, "unable to write on the sd card ");
		}
		fileList.clear();

		if (path.exists() && path.canRead()) {
			FilenameFilter filter = new FilenameFilter() {
				public boolean accept(File dir, String filename) {
					File sel = new File(dir, filename);
					boolean showReadableFile = showHiddenFilesAndDirs
							|| sel.canRead();
					if (currentAction == SELECT_DIRECTORY) {
						return (sel.isDirectory() && showReadableFile);
					}
					if (currentAction == SELECT_FILE) {
						if (sel.isFile() && filterFileExtension != null) {
							return (showReadableFile && sel.getName().endsWith(
									filterFileExtension));
						}
						return (showReadableFile);
					}
					return true;
				}
			};

			String[] fList = path.list(filter);
			this.directoryShownIsEmpty = false;
			for (int i = 0; i < fList.length; i++) {
				File sel = new File(path, fList[i]);
				Log.d(TAG,
						"File:" + fList[i] + " readable:"
								+ (Boolean.valueOf(sel.canRead())).toString());

				int drawableID = R.drawable.ic_insert_drive_file_black_34dp;
				boolean canRead = sel.canRead();
				if (sel.isDirectory()) {
					if (canRead) {
						drawableID = R.drawable.ic_folder_black_34dp;
					} else {
						drawableID = R.drawable.folder_icon_light;
					}
					fileList.add(i, new Item(fList[i], drawableID, null, canRead));
				} else {
					Uri contentUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName(), sel);
					String type = getContentResolver().getType(contentUri);
					Bitmap bitmap = null;
					assert type != null;
					if (type.equals("image/jpeg")) {
						try {
							bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), contentUri);
						} catch (Exception e) {
							e.printStackTrace();
						}
						bitmap = ThumbnailUtils.extractThumbnail(bitmap, 100, 120);
						fileList.add(i, new Item(fList[i], -1, bitmap, canRead));
					} else if (type.equals("audio/mpeg") || type.equals("audio/x-wav")) {
						drawableID = R.drawable.ic_headset_black_24dp;
						fileList.add(i, new Item(fList[i], drawableID, null, canRead));
					} else {
						fileList.add(i, new Item(fList[i], drawableID, null, canRead));
					}
				}
			}
			if (fileList.size() == 0) {
				this.directoryShownIsEmpty = true;
				fileList.add(0, new Item("Directory is empty", -1, null, true));
			} else {// sort non empty list
				Collections.sort(fileList, new ItemFileNameComparator());
			}
		} else {
			Log.e(TAG, "path does not exist or cannot be read");
		}
	}


	private void createFileListAdapter() {

		adapter = new ArrayAdapter<Item>(this,
				android.R.layout.select_dialog_item, android.R.id.text1,
				fileList) {
			@RequiresApi(api = Build.VERSION_CODES.DONUT)
			@NotNull
			@Override
			public View getView(int position, View convertView, @NotNull ViewGroup parent) {
				View view = super.getView(position, convertView, parent);
				TextView textView = (TextView) view
						.findViewById(android.R.id.text1);
				if (fileList.get(position).bitmap != null) {
					textView.setCompoundDrawablesWithIntrinsicBounds(
							new BitmapDrawable(getResources(), fileList.get(position).bitmap),
							null, null, null);
				} else {
					// put the image on the text view
					int drawableID = 0;
					if (fileList.get(position).icon != -1) {
						// If icon == -1, then directory is empty
						drawableID = fileList.get(position).icon;
					}
					textView.setCompoundDrawablesWithIntrinsicBounds(drawableID, 0,
							0, 0);
				}
				textView.setEllipsize(null);
				int dp5 = (int) (5 * getResources().getDisplayMetrics().density + 0.5f);
				textView.setCompoundDrawablePadding(dp5);
				textView.setBackgroundColor(Color.WHITE);
				return view;
			}
		};
	}

	private class Item {
		public String file;
		public int icon;
		public boolean canRead;
		public Bitmap bitmap;

		public Item(String file, Integer icon, Bitmap bitmap, boolean canRead) {
			this.file = file;
			this.icon = icon;
			this.bitmap = bitmap;
		}

		@Override
		public String toString() {
			return file;
		}
	}

	private class ItemFileNameComparator implements Comparator<Item> {
		public int compare(Item lhs, Item rhs) {
			return lhs.file.toLowerCase().compareTo(rhs.file.toLowerCase());
		}
	}

	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			Log.d(TAG, "ORIENTATION_LANDSCAPE");
		} else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
			Log.d(TAG, "ORIENTATION_PORTRAIT");
		}
	}

	@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
	public static long getFreeSpace(String path) {
		StatFs stat = new StatFs(path);
		return (long) stat.getAvailableBlocksLong()
				* (long) stat.getBlockSizeLong();
	}

	public static String formatBytes(long bytes) {
		String retStr = "";
		if (bytes > 1073741824) {// Add GB
			long gbs = bytes / 1073741824;
			retStr += (Long.valueOf(gbs)).toString() + "GB ";
			bytes = bytes - (gbs * 1073741824);
		}
		if (bytes > 1048576) {
			long mbs = bytes / 1048576;
			retStr += (Long.valueOf(mbs)).toString() + "MB ";
			bytes = bytes - (mbs * 1048576);
		}
		if (bytes > 1024) {
			long kbs = bytes / 1024;
			retStr += (Long.valueOf(kbs)).toString() + "KB";
			bytes = bytes - (kbs * 1024);
		} else
			retStr += (Long.valueOf(bytes)).toString() + " bytes";
		return retStr;
	}
}
