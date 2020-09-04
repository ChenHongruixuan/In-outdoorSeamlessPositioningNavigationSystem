package com.example.administrator.osmapitest.location.indoorloc.nfc;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.example.administrator.osmapitest.data.ClientPos;

import java.util.Arrays;
import java.util.Objects;

/**
 * NFC correction module
 * Encapsulating the basic methods of NFC operation
 * do related operation in the method of onNewIntent
 * The MainActivity inherits NfcBaseActivity to realize the function of NFC location
 *
 * @author Qchrx
 * @version 1.1
 * @date 2018/3/21
 */
public class NfcBaseActivity extends AppCompatActivity {
    protected NfcAdapter mNfcAdapter;
    private PendingIntent mPendingIntent;
    protected boolean isCanReadNfc = false;


    @Override
    protected void onStart() {
        super.onStart();
        // Here, the adapter needs to be retrieved again, otherwise the message cannot be obtained
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        // Once the NFC message is intercepted, the window is called through PendingIntent
        mPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, getClass()), 0);
    }

    @Override
    public void onNewIntent(Intent intent) {
        if (isCanReadNfc) {
            readNfcTag(intent);
        }
    }

    /**
     * Read text data in NFC
     */
    private void readNfcTag(Intent intent) {
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(
                    NfcAdapter.EXTRA_NDEF_MESSAGES);
            NdefMessage ndefMsgs[] = null;
            if (rawMsgs != null) {
                ndefMsgs = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++) {
                    ndefMsgs[i] = (NdefMessage) rawMsgs[i];
                }
            }
            try {
                if (ndefMsgs != null) {
                    // Read the location information encapsulated in NFC
                    NdefRecord record = ndefMsgs[0].getRecords()[0];
                    String mTagText = parseTextRecord(record);
                    String[] nfcPosData = Objects.requireNonNull(mTagText).split("，");
                    // Encapsulating the location information into intent and sent out by broadcast
                    ClientPos posData = new ClientPos(nfcPosData);
                    Intent posIntent = new Intent("locate");
                    posIntent.putExtra("pos_data", posData);
                    sendBroadcast(posIntent);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        // Set the processing priority over all other NFC processes
        if (mNfcAdapter != null)
            mNfcAdapter.enableForegroundDispatch(this, mPendingIntent, null, null);
    }


    @Override
    public void onPause() {
        super.onPause();

        if (mNfcAdapter != null)
            mNfcAdapter.disableForegroundDispatch(this);
    }

    /**
     * Parse nDef text data, starting from the third byte
     */
    public static String parseTextRecord(NdefRecord ndefRecord) {

        if (ndefRecord.getTnf() != NdefRecord.TNF_WELL_KNOWN) {
            return null;
        }

        if (!Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)) {
            return null;
        }
        try {
            // Get the byte array and analyze it
            byte[] payload = ndefRecord.getPayload();
            // start the first byte of NDEF text data, the status byte
            // judge whether the text is based on UTF-8 or utf-16,
            // take the "bit" of the first byte and the hexadecimal 80 of the "bit", which means the highest bit is 1,
            // the other bits are all zeros, so the highest bit is retained after the bitwise and operation

            String textEncoding = ((payload[0] & 0x80) == 0) ? "UTF-8" : "UTF-16";

            int languageCodeLength = payload[0] & 0x3f;

            String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");
            // The text is parsed according to the byte after nDef text data
            return new String(payload, languageCodeLength + 1,
                    payload.length - languageCodeLength - 1, textEncoding);
        } catch (Exception e) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * If permission is not allowed, cancel the program
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(this, "必须同意所有权限才能使用本程序",
                                    Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                } else {
                    Toast.makeText(this, "发生未知错误", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }
    }
}
