package org.totschnig.myexpenses.fragment;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import org.totschnig.myexpenses.MyApplication;
import org.totschnig.myexpenses.R;
import org.totschnig.myexpenses.activity.ProtectedFragmentActivity;
import org.totschnig.myexpenses.adapter.CurrencyAdapter;
import org.totschnig.myexpenses.dialog.EditCurrencyDialog;
import org.totschnig.myexpenses.model.CurrencyContext;
import org.totschnig.myexpenses.model.CurrencyUnit;
import org.totschnig.myexpenses.viewmodel.CurrencyViewModel;
import org.totschnig.myexpenses.viewmodel.data.Currency;

import java.util.Locale;

import javax.inject.Inject;

import static org.totschnig.myexpenses.dialog.EditCurrencyDialog.KEY_RESULT;
import static org.totschnig.myexpenses.provider.DatabaseConstants.KEY_CURRENCY;

public class CurrencyList extends ListFragment {
  private static final int EDIT_REQUEST = 1;
  private CurrencyViewModel currencyViewModel;
  private CurrencyAdapter currencyAdapter;

  @Inject
  CurrencyContext currencyContext;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    MyApplication.getInstance().getAppComponent().inject(this);
    setAdapter();
    currencyViewModel = ViewModelProviders.of(this).get(CurrencyViewModel.class);
    currencyViewModel.getCurrencies().observe(this, currencies -> {
      currencyAdapter.clear();
      currencyAdapter.addAll(currencies);
    });
    currencyViewModel.loadCurrencies();
  }

  private void setAdapter() {
    currencyAdapter = new CurrencyAdapter(getActivity(), android.R.layout.simple_list_item_1) {
      @NonNull
      @Override
      public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        TextView v = (TextView) super.getView(position, convertView, parent);
        Currency item = currencyAdapter.getItem(position);
        final CurrencyUnit currencyUnit = currencyContext.get(item.code());
        v.setText(String.format(Locale.getDefault(), "%s (%s, %d)", v.getText(),
            currencyUnit.symbol(),
            currencyUnit.fractionDigits()));
        return v;
      }
    };
    setListAdapter(currencyAdapter);
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == EDIT_REQUEST && resultCode == Activity.RESULT_OK) {
      currencyAdapter.notifyDataSetChanged();
      if (data != null) {
        int result = data.getIntExtra(KEY_RESULT, 0);
        ((ProtectedFragmentActivity) getActivity()).showSnackbar(
            getString(R.string.change_fraction_digits_result, result, data.getStringExtra(KEY_CURRENCY)), Snackbar.LENGTH_LONG);
      }
    }
  }

  @Override
  public void onListItemClick(ListView l, View v, int position, long id) {
    Currency item = currencyAdapter.getItem(position);
    final EditCurrencyDialog editCurrencyDialog = EditCurrencyDialog.newInstance(item);
    editCurrencyDialog.setTargetFragment(this, EDIT_REQUEST);
    editCurrencyDialog.show(getFragmentManager(), "SET_FRACTION_DIGITS");
  }
}
