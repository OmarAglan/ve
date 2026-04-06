package eu.pryds.ve;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

import eu.pryds.ve.GotoStringNumberDialogFragment.GotoStringNumberDialogListener;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Main editor screen for loading, editing, navigating and saving PO translations.
 */
public class MainActivity extends Activity implements GotoStringNumberDialogListener {
    
    private static final String PREF_STORAGE_NOTICE_SHOWN = "pref_storage_notice_shown";
    private TranslatableStringCollection str;
    private int currentString = 0;
    private int currentPluralForm = 0;
    private Menu menu;
    private File openedFile;
    private Uri openedFileUri;
    private static final int STORAGE_PERMISSION_REQUEST = 2;
    private static final String CONTENT_SCHEME = "content";
    public final static String CHOOSE_FILE_MESSAGE = "eu.pryds.ve.choosefile";
    private boolean hasShownStorageLegacyNotice = false;
    private Switch approvedSwitch;
    private TextView origStrView;
    private EditText translStrView;
    private TextView metadataView;
    private Button[] pluralButtons;
    private boolean suppressTranslationWatcher = false;
    private ActivityResultLauncher<Intent> chooseFileLauncher;
    private ActivityResultLauncher<Intent> chooseSafFileLauncher;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        hasShownStorageLegacyNotice = pref.getBoolean(PREF_STORAGE_NOTICE_SHOWN, false);
        approvedSwitch = (Switch) findViewById(R.id.approved);
        origStrView = (TextView) findViewById(R.id.orig_str);
        translStrView = (EditText) findViewById(R.id.transl_str);
        metadataView = (TextView) findViewById(R.id.metadata);
        pluralButtons = new Button[] {
                (Button) findViewById(R.id.plural0),
                (Button) findViewById(R.id.plural1),
                (Button) findViewById(R.id.plural2),
                (Button) findViewById(R.id.plural3),
                (Button) findViewById(R.id.plural4),
                (Button) findViewById(R.id.plural5)
        };

        approvedSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (str != null) {
                    TranslatableString currStr = str.getString(currentString);
                    currStr.setFuzzy(!isChecked);
                    updateMetadata();
                }
            }
        });
        
        origStrView.setMovementMethod(new ScrollingMovementMethod()); //make scrollable
        
        translStrView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void afterTextChanged(Editable editable) {
               if (suppressTranslationWatcher) {
                   return;
               }
               String changedText = translStrView.getText().toString();
               if (str != null) {
                   TranslatableString currStr = str.getString(currentString);
                   currStr.setTranslatedString(currentPluralForm, changedText);
                   updateMetadata();
               }
            }
        });
        
        metadataView.setMovementMethod(new ScrollingMovementMethod());

        chooseFileLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                this::handleLegacyFileChooserResult);
        chooseSafFileLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                this::handleSafFileChooserResult);
    }
    
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putParcelable("str", str);
        savedInstanceState.putInt("currentString", currentString);
        savedInstanceState.putInt("currentPluralForm", currentPluralForm);
        if (openedFileUri != null) {
            savedInstanceState.putString("openedFileUri", openedFileUri.toString());
        }
        
        super.onSaveInstanceState(savedInstanceState);
    }
    
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        
        if (savedInstanceState != null) {
            str = (TranslatableStringCollection) savedInstanceState.getParcelable("str");
            currentString = savedInstanceState.getInt("currentString");
            currentPluralForm = savedInstanceState.getInt("currentPluralForm");
            String openedFileUriString = savedInstanceState.getString("openedFileUri");
            if (openedFileUriString != null) {
                openedFileUri = Uri.parse(openedFileUriString);
            }
            
            updateScreen();
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_activity_actions, menu);
        
        if (str != null)
            enableInitiallyDisabledViews(true);
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
        case R.id.action_previous:
            currentString--;
            if (currentString < 0)
                currentString = str.size() - 1;
            currentPluralForm = 0;
            updateScreen();
            return true;
        case R.id.action_next:
            currentString++;
            if (currentString >= str.size())
                currentString = 0;
            currentPluralForm = 0;
            updateScreen();
            return true;
        case R.id.action_nextunfinished:
            currentString = str.getNextStringInNeedOfWork(currentString);
            currentPluralForm = 0;
            updateScreen();
            return true;
        case R.id.action_gotostringnumber:
            GotoStringNumberDialogFragment gotoStr = new GotoStringNumberDialogFragment();
            Bundle bundle = new Bundle();
            bundle.putInt(GotoStringNumberDialogFragment.STRING_COUNT, str.size());
            gotoStr.setArguments(bundle);
            gotoStr.show(getFragmentManager(), "gotostringnumber");
            return true;
        case R.id.action_settings:
            openSettings();
            return true;
        case R.id.action_about:
            //show about dialog
            AboutDialogFragment about = new AboutDialogFragment();
            about.show(getFragmentManager(), "AboutFragment");
            return true;
        /*case R.id.action_donate:
            Intent donateIntent = new Intent(this, DonateActivity.class);
            startActivity(donateIntent);
            return true;*/
        case R.id.action_load:
            str = new TranslatableStringCollection();
            if (shouldUseSafFileFlow()) {
                Intent safLoadIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                safLoadIntent.addCategory(Intent.CATEGORY_OPENABLE);
                safLoadIntent.setType("*/*");
                chooseSafFileLauncher.launch(safLoadIntent);
            } else {
                if (!ensureStoragePermission()) {
                    return true;
                }
                
                Intent loadIntent = new Intent(this, FileChooser.class);
                chooseFileLauncher.launch(loadIntent);
            }
            return true;
        case R.id.action_save:
            SharedPreferences pref =
                PreferenceManager.getDefaultSharedPreferences(this);
            String prefName = pref.getString("pref_name", "");
            String prefEmail = pref.getString("pref_email", "");
            String prefMaillist = pref.getString("pref_maillist", "");
            
            if (prefName.length() == 0 || prefEmail.length() == 0 ||
                    prefMaillist.length() == 0) {
                Toast.makeText(getApplicationContext(),
                        getResources().getText(R.string.not_all_fields_filled),
                        Toast.LENGTH_LONG).show();
                return true;
            }
            
            // Generate PO file syntax:
            String[] poLines = str.toPoFile(this); // TODO: In separate thread(?)

            if (!saveToCurrentLocation(poLines)) {
                return true;
            }
            Toast.makeText(getApplicationContext(),
                    getResources().getText(R.string.file_saved),
                    Toast.LENGTH_SHORT).show();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    private void showErrorMessage(int errorMsg, String optionalFileName) {
        // if optionalFileName is null or an empty string, ignore it
        boolean showFileName = (optionalFileName != null && optionalFileName.length() > 0);
        CharSequence errorMessage = getResources().getText(errorMsg);
        
        new AlertDialog.Builder(this)
        .setTitle(R.string.error)
        .setMessage(errorMessage + (showFileName ? '\n' + optionalFileName : "") )
        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // do nothing
            }
        })
        .show();
    }
    
    private void handleLegacyFileChooserResult(ActivityResult result) {
        if (result.getResultCode() != RESULT_OK || result.getData() == null) {
            return;
        }
        String filePath = result.getData().getStringExtra(CHOOSE_FILE_MESSAGE);
        if (filePath == null || filePath.length() == 0) {
            showErrorMessage(R.string.file_unknownerror, null);
            return;
        }

        File file = new File(filePath);
        if (!file.exists() || !file.canRead()) {
            int errorMsg = (!file.exists()
                    ? R.string.file_filenotexist : R.string.file_cannotreadfile);
            showErrorMessage(errorMsg, file.getName());
            return;
        }

        TranslatableStringCollection tempCollection = new TranslatableStringCollection();
        int parseResult = tempCollection.parse(file, this); // TODO: In a separate thread
        if (parseResult != TranslatableStringCollection.ERROR_NONE) {
            showParseError(parseResult);
            return;
        }

        str = tempCollection;
        openedFile = file;
        openedFileUri = null;
        updateScreen();
        enableInitiallyDisabledViews(true);
    }

    private void handleSafFileChooserResult(ActivityResult result) {
        Intent data = result.getData();
        if (result.getResultCode() != RESULT_OK || data == null || data.getData() == null) {
            return;
        }
        Uri fileUri = data.getData();
        Uri safeFileUri = toAcceptedSafUri(fileUri);
        if (safeFileUri == null) {
            showErrorMessage(R.string.file_unknownerror, null);
            return;
        }
        final int uriPermissionFlags = data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        try {
            getContentResolver().takePersistableUriPermission(safeFileUri, uriPermissionFlags);
        } catch (SecurityException e) {
            // Continue; temporary grant from chooser can still be sufficient for this session.
        }

        TranslatableStringCollection tempCollection = new TranslatableStringCollection();
        int parseResult;
        InputStream in = null;
        try {
            in = getContentResolver().openInputStream(safeFileUri);
            if (in == null) {
                showErrorMessage(R.string.file_ioerror, null);
                return;
            }
            try (InputStream stream = in) {
                parseResult = tempCollection.parse(stream, this);
            }
        } catch (IOException e) {
            showErrorMessage(R.string.file_ioerror, null);
            return;
        }

        if (parseResult != TranslatableStringCollection.ERROR_NONE) {
            showParseError(parseResult);
            return;
        }

        str = tempCollection;
        openedFileUri = safeFileUri;
        openedFile = null;
        updateScreen();
        enableInitiallyDisabledViews(true);
    }

    private void showParseError(int parseResult) {
        switch (parseResult) {
        case TranslatableStringCollection.ERROR_NOT_PO_FILE:
            showErrorMessage(R.string.file_notpofile, null);
            break;
        case TranslatableStringCollection.ERROR_FILE_EMPTY:
            showErrorMessage(R.string.file_fileempty, null);
            break;
        case TranslatableStringCollection.ERROR_FILE_NOT_FOUND:
            showErrorMessage(R.string.file_filenotexist, null);
            break;
        case TranslatableStringCollection.ERROR_IO:
            showErrorMessage(R.string.file_ioerror, null);
            break;
        default:
            showErrorMessage(R.string.file_unknownerror, null);
            break;
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_REQUEST) {
            boolean readGranted = false;
            boolean writeGranted = false;
            for (int i = 0; i < permissions.length && i < grantResults.length; i++) {
                if (Manifest.permission.READ_EXTERNAL_STORAGE.equals(permissions[i])) {
                    readGranted = grantResults[i] == PackageManager.PERMISSION_GRANTED;
                } else if (Manifest.permission.WRITE_EXTERNAL_STORAGE.equals(permissions[i])) {
                    writeGranted = grantResults[i] == PackageManager.PERMISSION_GRANTED;
                }
            }
            if (!(readGranted && writeGranted)) {
                Toast.makeText(getApplicationContext(),
                        getResources().getText(R.string.storage_permission_required),
                        Toast.LENGTH_LONG).show();
            }
        }
    }
    
    private boolean ensureStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (!hasShownStorageLegacyNotice) {
                Toast.makeText(getApplicationContext(),
                        getResources().getText(R.string.storage_legacy_mode_notice),
                        Toast.LENGTH_LONG).show();
                hasShownStorageLegacyNotice = true;
                SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
                pref.edit().putBoolean(PREF_STORAGE_NOTICE_SHOWN, true).apply();
            }
            return true;
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        boolean hasWrite = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        boolean hasRead = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        if (hasWrite && hasRead) {
            return true;
        }
        requestPermissions(new String[] {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        }, STORAGE_PERMISSION_REQUEST);
        return false;
    }
    
    @Override
    public void onReturnValue(int value) {
        // Returned value from GotoStringNumberDialogFragment
        currentString = value;
        currentPluralForm = 0;
        updateScreen();
    }
    
    /** Called when the user touches one of the plural form buttons */
    public void changePluralForm(View view) {
        switch (view.getId()) {
        case R.id.plural0:
            currentPluralForm = 0;
            updateScreen();
            return;
        case R.id.plural1:
            currentPluralForm = 1;
            updateScreen();
            return;
        case R.id.plural2:
            currentPluralForm = 2;
            updateScreen();
            return;
        case R.id.plural3:
            currentPluralForm = 3;
            updateScreen();
            return;
        case R.id.plural4:
            currentPluralForm = 4;
            updateScreen();
            return;
        case R.id.plural5:
            currentPluralForm = 5;
            updateScreen();
            return;
        default:
            return;
        }
    }
    
    private void updateScreen() {
        if (str == null)
            return;
        TranslatableString currentStr = str.getString(currentString);

        approvedSwitch.setChecked(!currentStr.isFuzzy());
        updatePluralButtons(currentStr);
        updateEditorFields(currentStr);
        updateMetadata();
    }
    
    private void updateMetadata() {
        TranslatableString currentStr = str.getString(currentString);
        int fuzzyCount = str.countFuzzyStrings();
        int untransCount = str.countUntranslatedStrings();
        
        metadataView.setText(
                getResources().getText(R.string.meta_str_no) + " " +
                (currentString+1) + "/" + str.size() +
                
                " (" +
                getResources().getQuantityString(R.plurals.meta_not_ready, fuzzyCount, fuzzyCount) +
                " " +
                getResources().getQuantityString(R.plurals.meta_untranslated, untransCount, untransCount) +
                ")" + '\n' +
                
                getResources().getText(R.string.meta_context) + " " +
                (currentStr.getContext().equals("") ?
                "" : currentStr.getContext()) + '\n' +
                
                getResources().getText(R.string.meta_notes) + " " +
                (currentStr.getTranslatorComments().equals("") ?
                "" : currentStr.getTranslatorComments() + '\n') +
                (currentStr.getExtractedComments().equals("") ?
                "" : currentStr.getExtractedComments()) + '\n' +
                
                getResources().getText(R.string.meta_files) + " " +
                (currentStr.getReferences().size() == 0 ?
                "" : currentStr.getReferencesAsString()) + '\n'
                );
    }

    private void updatePluralButtons(TranslatableString currentStr) {
        int pluralForms = str.getHeader().getHeaderPluralFormCount(this);
        boolean showPluralButtons = currentStr.isPluralString();
        for (int i = 0; i < pluralButtons.length; i++) {
            boolean visible = showPluralButtons && i < pluralForms;
            Button pluralButton = pluralButtons[i];
            pluralButton.setVisibility(visible ? Button.VISIBLE : Button.GONE);
            pluralButton.setSelected(visible && i == currentPluralForm);
            pluralButton.setEnabled(visible);
        }
    }

    private void updateEditorFields(TranslatableString currentStr) {
        origStrView.setText(currentPluralForm > 0 ?
                currentStr.getUntranslatedStringPlural() :
                currentStr.getUntranslatedString());

        String newTranslatedValue = currentStr.getTranslatedString(currentPluralForm);
        String existingValue = translStrView.getText().toString();
        if (!existingValue.equals(newTranslatedValue)) {
            int selectionStart = translStrView.getSelectionStart();
            int selectionEnd = translStrView.getSelectionEnd();
            suppressTranslationWatcher = true;
            translStrView.setText(newTranslatedValue);
            suppressTranslationWatcher = false;
            int newLength = translStrView.getText().length();
            int safeStart = Math.max(0, Math.min(selectionStart, newLength));
            int safeEnd = Math.max(0, Math.min(selectionEnd, newLength));
            translStrView.setSelection(safeStart, safeEnd);
        }
    }
    
    private void enableInitiallyDisabledViews(boolean enable) {
        updateNavigationState(enable);
        approvedSwitch.setEnabled(enable);
        origStrView.setEnabled(enable);
        translStrView.setEnabled(enable);
    }

    private void updateNavigationState(boolean enable) {
        if (menu == null) {
            return;
        }
        MenuItem actionPrev = menu.findItem(R.id.action_previous);
        actionPrev.setEnabled(enable);

        MenuItem actionNext = menu.findItem(R.id.action_next);
        actionNext.setEnabled(enable);

        MenuItem actionNextUnfinished = menu.findItem(R.id.action_nextunfinished);
        actionNextUnfinished.setEnabled(enable);

        MenuItem actionGotostringnumber = menu.findItem(R.id.action_gotostringnumber);
        actionGotostringnumber.setEnabled(enable);

        MenuItem actionSave = menu.findItem(R.id.action_save);
        actionSave.setEnabled(enable);
    }
    
    private void openSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    private boolean shouldUseSafFileFlow() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q;
    }

    private boolean saveToCurrentLocation(String[] poLines) {
        if (openedFileUri != null) {
            try {
                writePoLinesToUri(openedFileUri, poLines);
                return true;
            } catch (IOException e) {
                showErrorMessage(R.string.file_ioerror, null);
                return false;
            }
        }

        if (openedFile == null) {
            showErrorMessage(R.string.file_unknownerror, null);
            return false;
        }
        if (!openedFile.exists()) {
            showErrorMessage(R.string.file_filenotexist, openedFile.getName());
            return false;
        }
        if (!openedFile.canWrite()) {
            showErrorMessage(R.string.file_cannotwritefile, openedFile.getName());
            return false;
        }

        try {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(openedFile))) {
                writePoLines(writer, poLines);
            }
            return true;
        } catch (IOException e) {
            showErrorMessage(R.string.file_ioerror, openedFile.getName());
            return false;
        }
    }

    private void writePoLinesToUri(Uri uri, String[] poLines) throws IOException {
        try (OutputStream out = getContentResolver().openOutputStream(uri)) {
            if (out == null) {
                throw new IOException("Could not open output stream");
            }
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8))) {
                writePoLines(writer, poLines);
            }
        }
    }

    private void writePoLines(BufferedWriter writer, String[] poLines) throws IOException {
        for (int i = 0; i < poLines.length; i++) {
            writer.write(poLines[i]);
            writer.write('\n');
        }
        writer.flush();
    }

    private Uri toAcceptedSafUri(Uri fileUri) {
        if (fileUri == null) {
            return null;
        }
        String scheme = fileUri.getScheme();
        if (!CONTENT_SCHEME.equals(scheme)) {
            return null;
        }
        if (!DocumentsContract.isDocumentUri(this, fileUri)) {
            return null;
        }
        String documentId;
        try {
            documentId = DocumentsContract.getDocumentId(fileUri);
        } catch (IllegalArgumentException e) {
            return null;
        }
        if (documentId == null || documentId.length() == 0 || documentId.startsWith("/")) {
            return null;
        }
        String authority = fileUri.getAuthority();
        if (authority == null || authority.length() == 0) {
            return null;
        }
        return DocumentsContract.buildDocumentUri(authority, documentId);
    }
}
