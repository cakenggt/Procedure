/*
 * Copyright 2012 Terlici Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.aleclownes.procedure;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

public class DragNDropArrayAdapter<T> extends ArrayAdapter implements DragNDropAdapter {
    private int mPosition[];
    private int mHandler;
    private List<?> dataList;

    public DragNDropArrayAdapter(Context context, int resource, List<?> objects, int handler) {
        super(context, resource, objects);

        mHandler = handler;
        dataList = objects;
        setup(objects.size());
    }

    private void setup(int size) {
        mPosition = new int[size];

        for (int i = 0; i < size; ++i)
            mPosition[i] = i;
    }

    @Override
    public int getDragHandler() {
        return mHandler;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return super.areAllItemsEnabled();
    }

    @Override
    public boolean isEnabled(int position) {
        return super.isEnabled(mPosition[position]);
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        super.registerDataSetObserver(observer);
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        super.unregisterDataSetObserver(observer);
    }

    @Override
    public int getCount() {
        return dataList.size();
    }

    @Override
    public Object getItem(int position) {
        return super.getItem(mPosition[position]);
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(mPosition[position]);
    }

    @Override
    public boolean hasStableIds() {
        return super.hasStableIds();
    }

    @Override
    public View getView(int position, View view, ViewGroup group) {
        return super.getView(mPosition[position], view, group);
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(mPosition[position]);
    }

    @Override
    public int getViewTypeCount() {
        return super.getViewTypeCount();
    }

    @Override
    public boolean isEmpty() {
        return super.isEmpty();
    }

    @Override
    public void onItemDrag(DragNDropListView parent, View view, int position, long id) {

    }

    @Override
    public void onItemDrop(DragNDropListView parent, View view, int startPosition, int endPosition, long id) {
        int position = mPosition[startPosition];

        if (startPosition < endPosition)
            for (int i = startPosition; i < endPosition; ++i)
                mPosition[i] = mPosition[i + 1];
        else if (endPosition < startPosition)
            for (int i = startPosition; i > endPosition; --i)
                mPosition[i] = mPosition[i - 1];

        mPosition[endPosition] = position;
    }

    @Override
    public void notifyDataSetChanged() {
        setup(dataList.size());
        super.notifyDataSetChanged();
    }

    @Override
    public View getDropDownView(int position, View view, ViewGroup group) {
        return super.getDropDownView(mPosition[position], view, group);
    }

}