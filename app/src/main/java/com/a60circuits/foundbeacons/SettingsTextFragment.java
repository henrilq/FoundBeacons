package com.a60circuits.foundbeacons;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

/**
 * Created by zoz on 02/06/2016.
 */
public class SettingsTextFragment extends Fragment{

    public static final String FAQ = "faq";

    public static final String LEGAL_MENTION = "legal_mention";
    private ImageButton legalButton;
    private ImageButton faqButton;
    private TextView textView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.settings_text_fragment,container,false);
        Bundle args = getArguments();
        if(args != null){
            boolean isFaq = args.getBoolean(FAQ);
            boolean isLegalMention = args.getBoolean(LEGAL_MENTION);
            legalButton = (ImageButton) view.findViewById(R.id.legal_button);
            faqButton = (ImageButton) view.findViewById(R.id.faq_button);
            textView = (TextView) view.findViewById(R.id.text);

            legalButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switchToLegalMention();
                }
            });

            faqButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switchToFaq();
                }
            });
            if(isFaq){
                switchToFaq();
            }else if(isLegalMention){
                switchToLegalMention();
            }
        }
        return view;
    }

    private void switchToFaq(){
        legalButton.setColorFilter(null);
        faqButton.setColorFilter(ContextCompat.getColor(getContext(),R.color.colorSelectionBlue));
        textView.setText(getResources().getString(R.string.settings_faq));
    }

    private void switchToLegalMention(){
        faqButton.setColorFilter(null);
        legalButton.setColorFilter(ContextCompat.getColor(getContext(),R.color.colorSelectionBlue));
        textView.setText(getResources().getString(R.string.settings_legal_mention));
    }
}
