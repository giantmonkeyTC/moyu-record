package cn.troph.tomon.ui.fragments;

import android.util.SparseArray;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class TomonMainPagerAdapter extends FragmentStateAdapter {

    private FragmentSupplier mFragmentSupplier;
    private SparseArray<Fragment> mFragmentsCache;

    public TomonMainPagerAdapter(@NonNull FragmentActivity fragmentActivity, FragmentSupplier supplier) {
        super(fragmentActivity);
        if (supplier == null) {
            throw new IllegalArgumentException("You must initialize a FragmentSupplier instance for pager adapter!");
        }
        mFragmentSupplier = supplier;
        mFragmentsCache = new SparseArray<>(supplier.getFragmentNum());
    }

    public Fragment getFragment(int pos) {
        return mFragmentsCache.get(pos);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Fragment fragment = mFragmentSupplier.getFragmentByPosition(position);
        mFragmentsCache.append(position, fragment);
        return fragment;
    }

    @Override
    public int getItemCount() {
        return mFragmentSupplier.getFragmentNum();
    }

    public interface FragmentSupplier {
        Fragment getFragmentByPosition(int pos);
        int getFragmentNum();
    }
}
