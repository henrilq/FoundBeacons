package com.a60circuits.foundbeacons;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.a60circuits.foundbeacons.utils.LayoutUtils;
import com.a60circuits.foundbeacons.utils.ResourcesUtils;

/**
 * Created by zoz on 02/06/2016.
 */
public class SettingsTextFragment extends Fragment{

    public static final String FAQ = "faq";
    public static final String LEGAL_MENTION = "legal_mention";

    private ImageButton legalButton;
    private ImageButton faqButton;
    private Button closeButton;
    private TextView textView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.settings_text_fragment,container,false);
        MainActivity activity = (MainActivity) getActivity();
        activity.showArrow(true);
        Bundle args = getArguments();
        if(args != null){
            boolean isFaq = args.getBoolean(FAQ);
            boolean isLegalMention = args.getBoolean(LEGAL_MENTION);
            legalButton = (ImageButton) view.findViewById(R.id.legal_button);
            faqButton = (ImageButton) view.findViewById(R.id.faq_button);
            LayoutUtils.overLapView(legalButton,faqButton,true);
            closeButton = (Button) view.findViewById(R.id.close);
            textView = (TextView) view.findViewById(R.id.text);
            textView.setTypeface(ResourcesUtils.getTypeFace(getContext(), R.string.font_brandon_med));
            closeButton.setTypeface(ResourcesUtils.getTypeFace(getContext(), R.string.font_brandon_bld));

            closeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            });

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
        legalButton.setZ(0);
        faqButton.setZ(1);
    }

    private void switchToLegalMention(){
        faqButton.setColorFilter(null);
        legalButton.setColorFilter(ContextCompat.getColor(getContext(),R.color.colorSelectionBlue));
        textView.setText(R.string.settings_legal_mention_text);
        legalButton.setZ(1);
        faqButton.setZ(0);
    }
}
