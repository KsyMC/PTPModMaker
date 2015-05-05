package com.ksymc.ptpmodmaker;

import java.io.File;

import com.google.android.gms.ads.*;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends Activity implements OnClickListener, OnItemClickListener, OnItemLongClickListener {
	private AdView adView;
	
	ListView offsetlist;
	ListView valuelist;
	ArrayAdapter<String> offsetlistAdapter;
	ArrayAdapter<String> valuelistAdapter;

	TextView autherinfo, versioninfo;
	Button openfile, savefile, saveAsfile;

	File resetFile;
	File patchFile;
    boolean fileOpened, fileEdited;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		resetFile = new File(Environment.getExternalStorageDirectory().getAbsoluteFile(), "new.mod");
		patchFile = resetFile;

		offsetlistAdapter = new ArrayAdapter<String>(this, R.layout.values_row);
		valuelistAdapter = new ArrayAdapter<String>(this, R.layout.values_row);

		autherinfo = (TextView) findViewById(R.id.infoAutherText);
		versioninfo = (TextView) findViewById(R.id.infoVersionText);

		Button newfile = (Button) findViewById(R.id.newFile);
		newfile.setOnClickListener(this);

		Button addoffset = (Button) findViewById(R.id.addOffset);
		addoffset.setOnClickListener(this);

		openfile = (Button) findViewById(R.id.openFile);
		openfile.setOnClickListener(this);

		savefile = (Button) findViewById(R.id.saveFile);
		savefile.setOnClickListener(this);

		saveAsfile = (Button) findViewById(R.id.saveAsFile);
		saveAsfile.setOnClickListener(this);

		offsetlist = (ListView) findViewById(R.id.offsetList);
		offsetlist.setAdapter(offsetlistAdapter);
		offsetlist.setOnItemClickListener(this);
		offsetlist.setOnItemLongClickListener(this);

		valuelist = (ListView) findViewById(R.id.valueList);
		valuelist.setAdapter(valuelistAdapter);
		
		adView = new AdView(this);
		adView.setAdUnitId("ca-app-pub-4083161917191939/1906470405");
		adView.setAdSize(AdSize.BANNER);
		
		LinearLayout adLayout = (LinearLayout) findViewById(R.id.adLayout);
		adLayout.addView(adView);
		
		AdRequest request = new AdRequest.Builder()
			.addTestDevice("70C5A95EEADD182D5CBA7E3630DC2474")
			.build();
		
		adView.loadAd(request);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.newFile:
			if (fileEdited) {
				AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
				builder.setTitle("새로 만들기").setMessage("저장되지 않은 데이터가 있습니다. 정말로 새로 만드시겠습니까?");
				builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						reset();
					}
				});
				builder.setNegativeButton("아니오", null);
				builder.create().show();
	        } else {
	        	reset();
	        }
			break;
		case R.id.addOffset:
			final Dialog dialog = createDialog("오프셋 추가", "오프셋을 정확하게 입력해 주세요. ex) 000F1AE0", false);
			Button donebtn = (Button) dialog.findViewById(R.id.addButton);
			donebtn.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					EditText offset = (EditText) dialog.findViewById(R.id.offsetText);
					EditText value = (EditText) dialog.findViewById(R.id.valueText);
					String[] values = parseValue(offset.getText().toString(), value.getText().toString());
					if (values != null) {
						addListValue(values[0], values[1]);
						dialog.dismiss();
					}
				}
			});
			dialog.show();
			break;
		case R.id.openFile:
			Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
			intent.setType("file/*");
			startActivityForResult(Intent.createChooser(intent, ".mod 파일 선택"), REQUEST_CHOOSER_OPEN);
			break;
		case R.id.saveFile:
			if (!fileOpened) {
				saveAs();
	        } else {
	        	String auther = autherinfo.getText().toString();
	        	String version = versioninfo.getText().toString();
	            fileSave(patchFile, auther, version);
	        }
			break;
		case R.id.saveAsFile:
			saveAs();
			break;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View v, final int pos, long id) {
		final Dialog dialog = createDialog("오프셋 수정", "오프셋을 정확하게 입력해 주세요. ex) 000F1AE0", true);

		final EditText offset = (EditText) dialog.findViewById(R.id.offsetText);
		final EditText value = (EditText) dialog.findViewById(R.id.valueText);

		offset.setText(offsetlistAdapter.getItem(pos));
		value.setText(valuelistAdapter.getItem(pos));

		Button donebtn = (Button) dialog.findViewById(R.id.addButton);
		donebtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String[] values = parseValue(offset.getText().toString(), value.getText().toString());
				if (values != null) {
					editListValue(pos, values[0], values[1]);
					dialog.dismiss();
				}
			}
		});
		Button removebtn = (Button) dialog.findViewById(R.id.removeButton);
		removebtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				removeListValue(pos);
				dialog.dismiss();
			}
		});
		dialog.show();
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View v, final int pos, long id) {
		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
		builder.setTitle("위치 수정");
		builder.setPositiveButton("위로", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				upOrDownListValue(pos, true);
			}
		});
		builder.setNegativeButton("아래로", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				upOrDownListValue(pos, false);
			}
		});
		builder.create().show();
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			if (requestCode == REQUEST_CHOOSER_OPEN) {
				reset();
				fileOpen(new File(data.getData().getPath()));
			}
		}
	}

	@Override
	protected void onDestroy() {
		adView.destroy();
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		adView.pause();
		super.onPause();
	}

	@Override
	protected void onResume() {
		adView.resume();
		super.onResume();
	}

	public void saveAs() {
		final Dialog dialog = new Dialog(this);
		dialog.setTitle("파일 저장");
		dialog.setContentView(R.layout.savefile_dialog);
		dialog.show();

		final EditText authertext = (EditText) dialog.findViewById(R.id.autherText);
		final EditText versiontext = (EditText) dialog.findViewById(R.id.versionText);
		final EditText filenametext = (EditText) dialog.findViewById(R.id.fileNameText);

		authertext.setText(autherinfo.getText());
		versiontext.setText(versioninfo.getText());
		filenametext.setText(patchFile.getAbsolutePath());

		Button savebtn = (Button) dialog.findViewById(R.id.saveButton);
		savebtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String version = versiontext.getText().toString().trim();
				String auther = authertext.getText().toString().trim();
				String filepath = filenametext.getText().toString().trim();

				if (!version.equals("") && !auther.equals("") && !filepath.equals("")) {
					fileSave(new File(filepath), auther, version);
					dialog.dismiss();
				} else {
					showError(DATA_INPUT_ERROR);
				}
			}
		});
	}

	public String[] parseValue(String offset, String value) {
		String[] ret = new String[2];
		String offsetText = offset.trim().replaceAll("\\p{Space}", "").toUpperCase();
		String valueText = value.trim().replaceAll("\\p{Space}", "").toUpperCase();

		if (!offsetText.equals("") && !valueText.equals("")) {
			if (offsetText.length() != 8) {
				showError(OFFSET_INPUT_ERROR);
				return null;
			}
            ret[0] = offsetText;
            ret[1] = valueText;
            return ret;
        }
		showError(OFFSET_OR_VALUE_INPUT_ERROR);
        return null;
	}

	public void fileOpen(File file) {
		try {
            PTPatch patch = new PTPatch(file);
            if (patch.load()) {
				for (byte[] datas : patch.getData()) {
					String data = Utils.byteArrayToHex(datas);
					offsetlistAdapter.add(data.substring(0, 8).toUpperCase());
					valuelistAdapter.add(data.substring(8).toUpperCase());
				}

				autherinfo.setText(new String(patch.getAuther(), "UTF-8"));
                versioninfo.setText("" + patch.getMCPEVersion());

				setFileOpened(true, file);
                setFileEdited(false);
            } else {
                showError(HEADER_DAMAGED);
            }
        } catch (Exception e) {
        	e.printStackTrace();
        	showError(FILE_OPEN_ERROR);
        }
	}

	public void fileSave(File file, String auther, String version) {
		if (!file.getName().endsWith(".mod")) {
			file = new File(file.getAbsolutePath() + ".mod");
		}
        try {
			int size = offsetlistAdapter.getCount();
			byte[][] data = new byte[size][];
			for (int i = 0; i < size; i++) {
				String value = (String) offsetlistAdapter.getItem(i) + valuelistAdapter.getItem(i);
				data[i] = Utils.hexToByteArray(value);
			}

			PTPatch patch = new PTPatch(file);
            patch.write(Integer.parseInt(version), size, auther, data);

            autherinfo.setText(auther);
            versioninfo.setText(version);

            setFileOpened(true, file);
            setFileEdited(false);
        } catch(Exception e) {
        	e.printStackTrace();
            showError(FILE_SAVE_ERROR);
        }
	}

	public void addListValue(String offset, String value) {
		if (offsetlistAdapter.getPosition(offset) == -1) {
			offsetlistAdapter.add(offset);
			valuelistAdapter.add(value);

			setFileEdited(true);
        } else {
            showError(OFFSET_EXISTS);
        }
	}

	public void removeListValue(int index) {
        if (index != -1) {
        	valuelistAdapter.remove(valuelistAdapter.getItem(index));
        	offsetlistAdapter.remove(offsetlistAdapter.getItem(index));
        } else {
            showError(OFFSET_OR_VALUE_SELECTION_ERROR);
        }
    }

	public void editListValue(int index, String offset, String value) {
        if (offsetlistAdapter.getPosition(offset) == -1 || (offsetlistAdapter.getPosition(offset) != -1 && offsetlistAdapter.getItem(index).equals(offset))) {
        	offsetlistAdapter.remove(offsetlistAdapter.getItem(index));
        	valuelistAdapter.remove(valuelistAdapter.getItem(index));

        	offsetlistAdapter.insert(offset, index);
        	valuelistAdapter.insert(value, index);
        } else {
            showError(OFFSET_EXISTS);
        }
        setFileEdited(true);
    }

	public void upOrDownListValue(int index, boolean up) {
        if (index != -1) {
            if ((up && index != 0) || (!up && index != valuelistAdapter.getCount() - 1)) {
                String value = (String) valuelistAdapter.getItem(index);
                String offset = (String) offsetlistAdapter.getItem(index);
                valuelistAdapter.remove(value);
                offsetlistAdapter.remove(offset);
                if (up) index--;
                else index++;

            	offsetlistAdapter.insert(offset, index);
            	valuelistAdapter.insert(value, index);
                setFileEdited(true);
            } else {
                showError(UP_DOWN_ERROR);
            }
        } else {
        	showError(OFFSET_OR_VALUE_SELECTION_ERROR);
        }
    }

	public void reset() {
		autherinfo.setText("");
		versioninfo.setText("");
		offsetlistAdapter.clear();
		valuelistAdapter.clear();
		setFileOpened(false, resetFile);
		setFileEdited(false);
	}

    public void setFileEdited(boolean edited) {
		fileEdited = edited;
		savefile.setEnabled(edited);
        if (fileOpened) {
            setTitle((edited ? "*" : "") + patchFile.getName()  + " - " + "PTP Mod Maker");
        } else {
            setTitle("PTP Mod Maker");
        }
    }

	public void setFileOpened(boolean opened, File file) {
		patchFile = file;
		fileOpened = opened;
		saveAsfile.setEnabled(opened);
	}

	public Dialog createDialog(String titletext, String msgtext, boolean editmode) {
		Dialog dialog = new Dialog(this);
		dialog.setTitle(titletext);
		dialog.setContentView(R.layout.input_dialog);
		TextView message = (TextView) dialog.findViewById(R.id.messageText);
		Button remove = (Button) dialog.findViewById(R.id.removeButton);
		message.setText(msgtext);
		remove.setEnabled(editmode);
		return dialog;
	}

	private void showError(int code) {
        String message = "";
        switch(code) {
            case UNKNOWN_ERROR:
                message = "알 수 없는 오류 발생.";
                break;
            case FILE_SAVE_ERROR:
                message = "파일을 저장 할 수 없습니다.";
                break;
            case FILE_OPEN_ERROR:
                message = "파일을 열 수 없습니다.";
                break;
            case OFFSET_OR_VALUE_INPUT_ERROR:
                message = "오프셋 또는 값을 입력해 주세요.";
                break;
            case OFFSET_OR_VALUE_SELECTION_ERROR:
                message = "오프셋 또는 값을 선택해 주세요.";
                break;
            case UP_DOWN_ERROR:
                message = "더이상 올리거나 내릴 수 없습니다.";
                break;
            case OFFSET_EXISTS:
                message = "이미 같은 오프셋이 존재합니다!";
                break;
            case HEADER_DAMAGED:
                message = "헤더가 손상되었습니다.";
                break;
            case DATA_INPUT_ERROR:
                message = "모든 정보가 입력되지 않았습니다!";
                break;
			case OFFSET_INPUT_ERROR:
                message = "오프셋을 정확하게 입력해 주세요! ex) 000F1AE0";
                break;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("오류").setMessage(message);
        builder.setPositiveButton("확인", null);
        builder.create().show();
    }

	private static final int REQUEST_CHOOSER_OPEN = 1000;

    // showError
    public static final int UNKNOWN_ERROR = 0;
    public static final int FILE_SAVE_ERROR = 1;
    public static final int FILE_OPEN_ERROR = 2;
    public static final int OFFSET_OR_VALUE_INPUT_ERROR = 4;
    public static final int OFFSET_OR_VALUE_SELECTION_ERROR = 5;
    public static final int UP_DOWN_ERROR = 6;
    public static final int OFFSET_EXISTS = 7;
    public static final int HEADER_DAMAGED = 8;
    public static final int DATA_INPUT_ERROR = 9;
	public static final int OFFSET_INPUT_ERROR = 10;
}
