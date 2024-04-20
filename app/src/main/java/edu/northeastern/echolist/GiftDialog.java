package edu.northeastern.echolist;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
public class GiftDialog extends DialogFragment {

    private static final String ARG_GIFT_ID = "giftId";
    private static final String ARG_GIFT_NAME = "giftName";
    private static final String ARG_DESCRIPTION = "description";
    private static final String ARG_BUTTON_TEXT = "buttonText";

    private String giftId;
    private String giftName;

    private OnButtonClickListener onButtonClickListener;

    public static GiftDialog newInstance(String giftId, String giftName, String description, String buttonText, OnButtonClickListener onButtonClickListener) {
        GiftDialog dialog = new GiftDialog();
        Bundle args = new Bundle();
        args.putString(ARG_GIFT_ID, giftId);
        args.putString(ARG_GIFT_NAME, giftName);
        args.putString(ARG_DESCRIPTION, description);
        args.putString(ARG_BUTTON_TEXT, buttonText);
        dialog.setArguments(args);
        dialog.setOnButtonClickListener(onButtonClickListener);
        return dialog;
    }

    public void setOnButtonClickListener(OnButtonClickListener onButtonClickListener) {
        this.onButtonClickListener = onButtonClickListener;
    }
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_gift_details, null);

        giftId = getArguments().getString(ARG_GIFT_ID);
        giftName = getArguments().getString(ARG_GIFT_NAME);
        String description = getArguments().getString(ARG_DESCRIPTION);
        String buttonText = getArguments().getString(ARG_BUTTON_TEXT);

        TextView giftNameTextView = view.findViewById(R.id.gift_name_textview);
        TextView descriptionTextView = view.findViewById(R.id.description_textview);
        Button actionButton = view.findViewById(R.id.action_button);

        giftNameTextView.setText(giftName);
        descriptionTextView.setText(description);
        actionButton.setText(buttonText);

        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onButtonClickListener != null) {
                    onButtonClickListener.onButtonClick(giftId, giftName);
                }
            }
        });

        builder.setView(view)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

        return builder.create();
    }

    public interface OnButtonClickListener {
        void onButtonClick(String giftId, String giftName);
    }

    public interface OnDismissListener {
        void onDialogDismiss();
    }

    private OnDismissListener onDismissListener;

    public void setOnDismissListener(OnDismissListener onDismissListener) {
        this.onDismissListener = onDismissListener;
    }

    @Override
    public void onDismiss(DialogInterface dialogInterface) {
        super.onDismiss(dialogInterface);
        if (onDismissListener != null) {
            onDismissListener.onDialogDismiss();
        }
    }
}
