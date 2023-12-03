package fpoly.mds.beeshoes.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import fpoly.mds.beeshoes.databinding.ItemPhotoBinding;

public class PhotoAdapter extends PagerAdapter {
    private final Context context;
    private final ArrayList<String> list;

    public PhotoAdapter(Context context, ArrayList<String> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        ItemPhotoBinding binding = ItemPhotoBinding.inflate(LayoutInflater.from(context), container, false);
        try {
            Glide.with(context).load(list.get(position)).into(binding.imgPhoto);
        } catch (Exception e) {
        }
        container.addView(binding.getRoot());
        return binding.getRoot();
    }

    @Override
    public int getCount() {
        if (list != null) {
            return list.size();
        }
        return 0;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
      container.removeView((View) container);
    }
}
