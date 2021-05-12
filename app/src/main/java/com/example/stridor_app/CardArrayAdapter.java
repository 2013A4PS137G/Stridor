package com.example.stridor_app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Listview ArrayAdapter for the Card class
 */
public class CardArrayAdapter  extends ArrayAdapter<Card> {
    private static final String TAG = "CardArrayAdapter";
    private List<Card> cardList = new ArrayList<Card>();
    private int viewResourceId;

    /**
     * constructor
     * @param context
     * @param viewResourceId
     */
    public CardArrayAdapter(Context context, int viewResourceId) {
        super(context, viewResourceId);
        this.viewResourceId = viewResourceId;
    }

    /**
     * Adds a Card object into the cardList
     * @param c
     */
    @Override
    public void add(Card c) {
        cardList.add(c);
        super.add(c);
    }

    /**
     * Gets number of Cards in cardList
     */
    @Override
    public int getCount() {
        return this.cardList.size();
    }

    /**
     * Gets of Cards at an index from cardList
     *
     * @param index
     * @return
     */
    @Override
    public Card getItem(int index) {
        return this.cardList.get(index);
    }

    /**
     * Associates a Card with a view. if view is null it creates
     * @param position
     * @param convertView
     * @param parent
     * @return
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(this.viewResourceId, parent, false);
        }
        Card card = getItem(position);
        card.setView(row);
        return row;
    }

    void onFinishInflate(){

    }

}