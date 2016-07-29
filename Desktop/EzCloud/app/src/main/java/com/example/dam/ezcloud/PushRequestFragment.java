package com.example.dam.ezcloud;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by dam on 25/7/16.
 */
// TODO RECEIVE PUSH REQUEST
// Create chat Database
public class PushRequestFragment extends MyBasicFragment
{
	private static final String TAG = PushRequestFragment.class.getSimpleName();

	public class SingleMessage
	{
		String sender;
		String details;
		String repo_sender;
		String repo_receiver;
		String time;
		int isRead;
		public SingleMessage(JSONObject jsonObject) throws JSONException
		{
			sender = jsonObject.getString("sender");
			repo_sender = jsonObject.getString("repo_sender_name");
			repo_receiver = jsonObject.getString("repo_receiver_name");
			details = jsonObject.getString("details");
			time = jsonObject.getString("message_time");
			isRead = jsonObject.getInt("isRead");
		}
	}

	public PushRequestFragment(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState, Context context)
	{
		super(inflater, container, savedInstanceState, context);
	}
	ArrayList <SingleMessage> arrayList ;
	ListView listView;
	@Override
	public View onCreate()
	{
		View rootView = getRootView(R.layout.push_request);
		listView = (ListView) rootView.findViewById(R.id.list_view_container);
		HashMap<String,String> hashMap = new HashMap<>(1);
		hashMap.put("receiver",Home.userName);
		arrayList = new ArrayList<>();

		PostRequestSend postRequestSend = new PostRequestSend("http://ezcloud.esy.es/ezCloudWebsite/receiveMessages.php?",hashMap);
		postRequestSend.setTaskDoneListener(new PostRequestSend.TaskDoneListener()
		{
			@Override
			public String onTaskDone(String str) throws JSONException
			{
				Log.e(TAG, "onTaskDone: " + str );
				JSONArray jsonArray = new JSONArray(str);
				for (int i =  0 ; i <  jsonArray.length();i++)
				{
					arrayList.add(new SingleMessage(jsonArray.getJSONObject(i)));
				}
				listView.setAdapter(new MyAdapter());
				return null;
			}
		});
		postRequestSend.execute();

		return rootView;
	}
	public class MyAdapter extends BaseAdapter{
		public class MyView
		{
			TextView sender , details , timeOfMessage , isRead;
			Button commit , download;
		}
		@Override
		public int getCount()
		{
			return arrayList.size();
		}
		@Override
		public SingleMessage getItem(int position)
		{
			return arrayList.get(position);
		}
		@Override
		public long getItemId(int position)
		{
			return 0;
		}
		@Override
		public View getView(final int position, View convertView, ViewGroup parent)
		{
			LayoutInflater layoutInflater =  (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
			final MyView myView;
			if (convertView == null)
			{
				convertView = layoutInflater.inflate(R.layout.messages_received,null);
				myView = new MyView();
				myView.sender = (TextView) convertView.findViewById(R.id.text_view_from);
				myView.details = (TextView) convertView.findViewById(R.id.text_view_details);
				myView.commit = (Button) convertView.findViewById(R.id.btn_commit);
				myView.download = (Button) convertView.findViewById(R.id.btn_download);
				myView.timeOfMessage = (TextView) convertView.findViewById(R.id.text_view_time);
				myView.isRead = (TextView) convertView.findViewById(R.id.text_view_is_read);
				convertView.setTag(myView);
			}
			else
			{
				myView = (MyView) convertView.getTag();
			}
			myView.details.setText(getItem(position).details);
			myView.sender.setText(getItem(position).sender);
			myView.timeOfMessage.setText(getItem(position).time);
			myView.isRead.setText(String.valueOf(getItem(position).isRead == 1));
			myView.download.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{

					final String[] ver = new String[1];
					HashMap <String,String> hashMap = new HashMap<String, String>();
					hashMap.put("username",myView.sender.getText().toString());
					hashMap.put("reponame",getItem(position).repo_receiver);
					PostRequestSend postRequestSend = new PostRequestSend("http://ezcloud.esy.es/ezCloudWebsite/get_repo_version.php?",hashMap);
					postRequestSend.setTaskDoneListener(new PostRequestSend.TaskDoneListener()
					{
						@Override
						public String onTaskDone(String str) throws JSONException
						{
							ver[0] = str;
							String getFileName = getItem(position).repo_receiver + "/" + str + "/" + getItem(position).repo_receiver;
							DownloadFileFTP downloadFileFTP = new DownloadFileFTP(context, getFileName, myView.sender.getText().toString(), new DownloadFileFTP.OnFileDownloadListener()
							{
								@Override
								public void onFileDownload(String path)
								{
									Log.e(TAG, "onFileDownload: " + path );
									ZipToDirectory zipToDirectory = new ZipToDirectory();
									zipToDirectory.execute(path, Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+getItem(position).repo_receiver);

									Toast.makeText(context, path, Toast.LENGTH_SHORT).show();
								}
							});
							downloadFileFTP.execute();
							return null;
						}
					});
					postRequestSend.execute();

				}
			});
			myView.commit.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					//TODO IMPLEMENT COMMIT FILE . . . . . . .
				}
			});
			return convertView;
		}
	}
}
