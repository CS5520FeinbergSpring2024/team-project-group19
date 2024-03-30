package edu.northeastern.echolist;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
public class GiftDialog extends DialogFragment {

        private static final String ARG_DESCRIPTION = "description";

        public static GiftDialog newInstance(String description) {
            GiftDialog dialog = new GiftDialog();
            Bundle args = new Bundle();
            args.putString(ARG_DESCRIPTION, description);
            dialog.setArguments(args);
            return dialog;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            LayoutInflater inflater = requireActivity().getLayoutInflater();

            String description = getArguments().getString(ARG_DESCRIPTION);

            builder.setTitle("Details")
                    .setMessage(description)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });

            return builder.create();
        }
}
