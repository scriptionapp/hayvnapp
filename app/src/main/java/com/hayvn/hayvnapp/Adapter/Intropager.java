package com.hayvn.hayvnapp.Adapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.hayvn.hayvnapp.Fragment.FragmentIntro1;
import com.hayvn.hayvnapp.Fragment.FragmentIntro2;
import com.hayvn.hayvnapp.Fragment.FragmentIntro3;


public class Intropager extends FragmentPagerAdapter {


    public Intropager(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int index) {
        switch (index) {
            case 0:
                return new FragmentIntro1();
            case 1:
                return new FragmentIntro2();
            case 2:
                return new FragmentIntro3();
        }

        return null;
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "";
    }
}
