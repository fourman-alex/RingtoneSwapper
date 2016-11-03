package com.myexample.ringtoneswap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class FirebaseHelper {

	private static final String BUCKET_URL = "gs://ringtonswapper.appspot.com";
	private static FirebaseHelper sFirebaseHelper;

	public static FirebaseHelper getInstance(){
		if (sFirebaseHelper == null)
			sFirebaseHelper = new FirebaseHelper();

		return sFirebaseHelper;
	}

	private FirebaseHelper(){}

	public void init(OnCompleteListener<AuthResult> onCompleteListener){
		FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
		FirebaseAuth.AuthStateListener authListener = new FirebaseAuth.AuthStateListener() {
			@Override
			public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
				FirebaseUser user = firebaseAuth.getCurrentUser();
				if (user != null) {
					// User is signed in
					Log.d("downloadRingtone", "onAuthStateChanged:signed_in:" + user.getUid());
				}
				else {
					// User is signed out
					Log.d("downloadRingtone", "onAuthStateChanged:signed_out");
				}
			}
		};

		firebaseAuth.addAuthStateListener(authListener);

		firebaseAuth.signInAnonymously()
		    .addOnCompleteListener(onCompleteListener);
	}

	public void addNewUser(String phoneNumber){
		FirebaseDatabase database = FirebaseDatabase.getInstance();
		DatabaseReference myRef = database.getReference();
		myRef.child(phoneNumber).setValue(System.currentTimeMillis());
	}

	public void registerToRingtoneUpdates(String phoneNumber) {
		final FirebaseDatabase database = FirebaseDatabase.getInstance();
		DatabaseReference myRef = database.getReference(phoneNumber);
		myRef.addChildEventListener(new ChildEventListener() {
			@Override
			public void onChildAdded(DataSnapshot dataSnapshot, String s) {
				Log.d("MainActivity", "Adding ringtone for " + dataSnapshot.getKey());
			}

			@Override
			public void onChildChanged(DataSnapshot dataSnapshot, String s) {
				Log.d("MainActivity", "Updating ringtone for " + dataSnapshot.getKey() + "; timestamp: " + dataSnapshot.getValue(Long.class));
			}

			@Override
			public void onChildRemoved(DataSnapshot dataSnapshot) {

			}

			@Override
			public void onChildMoved(DataSnapshot dataSnapshot, String s) {

			}

			@Override
			public void onCancelled(DatabaseError databaseError) {

			}
		});
	}

	public void uploadRingtone(final String prankerPhoneNumber, final String prankeePhoneNumber, InputStream stream) throws
			FileNotFoundException {

		final String fileName = prankeePhoneNumber + "_" + prankerPhoneNumber + ".mp3";

		FirebaseStorage storage = FirebaseStorage.getInstance();
		StorageReference storageRef = storage.getReferenceFromUrl(BUCKET_URL);
		StorageReference pathReference = storageRef.child(fileName);

		UploadTask uploadTask = pathReference.putStream(stream);
		uploadTask.addOnFailureListener(new OnFailureListener() {
			@Override
			public void onFailure(@NonNull Exception exception) {
				Log.d("uploadRingtone", "upload failed");
				exception.printStackTrace();
			}
		}).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
			@Override
			public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
				FirebaseDatabase database = FirebaseDatabase.getInstance();
				DatabaseReference myRef = database.getReference(prankeePhoneNumber);
				myRef.child(prankerPhoneNumber).setValue(System.currentTimeMillis());

			}
		});

	}

	public void downloadAndSetRingtone(final String prankerPhoneNumber, String prankeePhoneNumber, final Context context) {

		final String fileName = prankeePhoneNumber + "_" + prankerPhoneNumber + ".mp3";
	    FirebaseStorage storage = FirebaseStorage.getInstance();
	    StorageReference storageRef = storage.getReferenceFromUrl(BUCKET_URL);
	    StorageReference pathReference = storageRef.child(fileName);

	    final File localFile = new File(context.getFilesDir(), fileName);
	    pathReference.getFile(localFile)
	                 .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
		                 @Override
		                 public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
			                 Log.d("downloadRingtone", "Download success");
							setRingtone(prankerPhoneNumber, localFile, context);
		                 }
	                 })
	                 .addOnFailureListener(new OnFailureListener() {
		                 @Override
		                 public void onFailure(@NonNull Exception exception) {
			                 Log.d("downloadRingtone", "Download failed");
			                 exception.printStackTrace();
		                 }
	                 });
	}

	public void setRingtone(String phoneNumber, File ringtone, Context context){
		// The Uri used to look up a contact by phone number
		final Uri lookupUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, phoneNumber);
		// The columns used for `Contacts.getLookupUri`
		final String[] projection = new String[] {
				ContactsContract.Contacts._ID, ContactsContract.Contacts.LOOKUP_KEY
		};
		// Build your Cursor
		final Cursor data = context.getContentResolver().query(lookupUri, projection, null, null, null);
		data.moveToFirst();
		try {
			// Get the contact lookup Uri
			final long contactId = data.getLong(0);
			final String lookupKey = data.getString(1);
			final Uri contactUri = ContactsContract.Contacts.getLookupUri(contactId, lookupKey);
			if (contactUri == null) {
				// Invalid arguments
				return;
			}

			final String value = Uri.fromFile(ringtone).toString();

			// Apply the custom ringtone
			final ContentValues values = new ContentValues(1);
			values.put(ContactsContract.Contacts.CUSTOM_RINGTONE, value);
			context.getContentResolver().update(contactUri, values, null, null);
		} finally {
			// Don't forget to close your Cursor
			data.close();
		}

	}
}
