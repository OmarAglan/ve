package eu.pryds.ve;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;
import androidx.fragment.app.DialogFragment;

public class GotoStringNumberDialogFragment extends DialogFragment {
    protected NumberPicker strNumberPicker;
    public static final String STRING_COUNT = "StringCount";
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        int stringCount = bundle.getInt(STRING_COUNT);
        
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.goto_string_number_picker, null);
        
        strNumberPicker = (NumberPicker) v.findViewById(R.id.goto_string_number_picker);
        // Set min and max values of picker:
        // Remember that picker shows values 1 to stringCount to user
        // while internal representation uses 0 to stringCount-1
        strNumberPicker.setMaxValue(stringCount < 1 ? 1 : stringCount);
        strNumberPicker.setMinValue(1);
        
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.action_gotostringnumber)
               .setView(v)
               .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       // no action, just close the dialog
                   }
               })
               .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int which) {
                       // Return the value of the number picker:
                       // (subtracted by 1, since internal representation starts at 0, not 1)
                       GotoStringNumberDialogListener activity =
                               (GotoStringNumberDialogListener) getActivity();
                       activity.onReturnValue(strNumberPicker.getValue() - 1);
                   }
               });
        
        return builder.create();
    }
    
    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface GotoStringNumberDialogListener {
        public void onReturnValue(int value);
    }
    
    // Use this instance of the interface to deliver action events
    GotoStringNumberDialogListener mListener;
    
    // Override the Fragment.onAttach() method to instantiate the GotoStringNumberDialogListener
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (GotoStringNumberDialogListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(context.toString()
                    + " must implement GotoStringNumberDialogListener");
        }
    }
}
