package com.sprtcoding.commutech.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.sprtcoding.commutech.R;

public class ViewPagerAdapter extends PagerAdapter {
    Context context;
    int images[] = {
            R.drawable.about_us,
            R.drawable.download_img,
            R.drawable.create_account_img,
            R.drawable.featured_img,
            R.drawable.scan_qr_img,
            R.drawable.driver_information_img,
            R.drawable.emergency_txt_img,
            R.drawable.location_track_img
    };

    int heading[] = {
            R.string.about_us_heading,
            R.string.heading_1,
            R.string.heading_2,
            R.string.heading_3,
            R.string.heading_4,
            R.string.heading_5,
            R.string.heading_6,
            R.string.heading_7
    };
    int description[] = {
            R.string.about_us,
            R.string.desc_1,
            R.string.desc_2,
            R.string.desc_3,
            R.string.desc_4,
            R.string.desc_5,
            R.string.desc_6,
            R.string.desc_7
    };

    public ViewPagerAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return heading.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == (LinearLayout) object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = layoutInflater.inflate(R.layout.slider_layout, container, false);

        ImageView slideImg = (ImageView) v.findViewById(R.id.titleImg);
        TextView titleText = (TextView) v.findViewById(R.id.textTitle);
        TextView textDesc = (TextView) v.findViewById(R.id.textDescription);

        slideImg.setImageResource(images[position]);
        titleText.setText(heading[position]);
        textDesc.setText(description[position]);

        container.addView(v);

        return v;

    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((LinearLayout)object);
    }
}
