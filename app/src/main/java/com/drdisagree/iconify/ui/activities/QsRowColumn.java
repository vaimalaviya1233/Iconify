package com.drdisagree.iconify.ui.activities;

import static com.drdisagree.iconify.common.Const.SYSTEMUI_PACKAGE;
import static com.drdisagree.iconify.common.Preferences.ALERT_DIALOG_QSROWCOL;
import static com.drdisagree.iconify.common.Preferences.QS_ROW_COLUMN_SWITCH;
import static com.drdisagree.iconify.common.References.FABRICATED_QQS_ROW;
import static com.drdisagree.iconify.common.References.FABRICATED_QQS_TILE;
import static com.drdisagree.iconify.common.References.FABRICATED_QS_COLUMN;
import static com.drdisagree.iconify.common.References.FABRICATED_QS_ROW;
import static com.drdisagree.iconify.common.References.FABRICATED_QS_TILE;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.databinding.ActivityQsRowColumnBinding;
import com.drdisagree.iconify.ui.utils.ViewHelper;
import com.drdisagree.iconify.ui.views.LoadingDialog;
import com.drdisagree.iconify.utils.overlay.FabricatedUtil;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.slider.Slider;

public class QsRowColumn extends BaseActivity {

    private ActivityQsRowColumnBinding binding;
    private LoadingDialog loadingDialog;

    public static void applyRowColumn() {
        FabricatedUtil.buildAndEnableOverlays(
                FABRICATED_QQS_ROW, SYSTEMUI_PACKAGE, "integer", "quick_qs_panel_max_rows", String.valueOf(Prefs.getInt(FABRICATED_QQS_ROW, 2)),
                FABRICATED_QS_ROW, SYSTEMUI_PACKAGE, "integer", "quick_settings_max_rows", String.valueOf(Prefs.getInt(FABRICATED_QS_ROW, 4)),
                FABRICATED_QS_COLUMN, SYSTEMUI_PACKAGE, "integer", "quick_settings_num_columns", String.valueOf(Prefs.getInt(FABRICATED_QS_COLUMN, 2)),
                FABRICATED_QQS_TILE, SYSTEMUI_PACKAGE, "integer", "quick_qs_panel_max_tiles", String.valueOf((Prefs.getInt(FABRICATED_QQS_ROW, 2) * Prefs.getInt(FABRICATED_QS_COLUMN, 2))),
                FABRICATED_QS_TILE, SYSTEMUI_PACKAGE, "integer", "quick_settings_min_num_tiles", String.valueOf((Prefs.getInt(FABRICATED_QS_COLUMN, 2) * Prefs.getInt(FABRICATED_QS_ROW, 4)))
        );
    }

    public static void resetRowColumn() {
        FabricatedUtil.disableOverlays(FABRICATED_QQS_ROW, FABRICATED_QS_ROW, FABRICATED_QS_COLUMN, FABRICATED_QQS_TILE, FABRICATED_QS_TILE);
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityQsRowColumnBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Header
        ViewHelper.setHeader(this, binding.header.toolbar, R.string.activity_title_qs_row_column);

        // Loading dialog while enabling or disabling pack
        loadingDialog = new LoadingDialog(this);

        // Quick QsPanel Row
        final int[] finalQqsRow = {Prefs.getInt(FABRICATED_QQS_ROW, 2)};
        binding.qqsRowOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + finalQqsRow[0]);
        binding.qqsRowSeekbar.setValue(finalQqsRow[0]);
        binding.qqsRowSeekbar.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                finalQqsRow[0] = (int) slider.getValue();
                binding.qqsRowOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + finalQqsRow[0]);
            }
        });

        // QsPanel Row
        final int[] finalQsRow = {Prefs.getInt(FABRICATED_QS_ROW, 4)};
        binding.qsRowOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + finalQsRow[0]);
        binding.qsRowSeekbar.setValue(finalQsRow[0]);
        binding.qsRowSeekbar.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                finalQsRow[0] = (int) slider.getValue();
                binding.qsRowOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + finalQsRow[0]);
            }
        });

        // QsPanel Column
        final int[] finalQsColumn = {Prefs.getInt(FABRICATED_QS_COLUMN, 2)};
        binding.qsColumnOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + finalQsColumn[0]);
        binding.qsColumnSeekbar.setValue(finalQsColumn[0]);
        binding.qsColumnSeekbar.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                finalQsColumn[0] = (int) slider.getValue();
                binding.qsColumnOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + finalQsColumn[0]);
            }
        });

        // Apply button
        binding.qsRowColumnApply.setOnClickListener(v -> {
            // Show loading dialog
            loadingDialog.show(getResources().getString(R.string.loading_dialog_wait));

            Runnable runnable = () -> {
                Prefs.putBoolean(QS_ROW_COLUMN_SWITCH, true);

                Prefs.putInt(FABRICATED_QQS_ROW, finalQqsRow[0]);
                Prefs.putInt(FABRICATED_QS_ROW, finalQsRow[0]);
                Prefs.putInt(FABRICATED_QS_COLUMN, finalQsColumn[0]);
                Prefs.putInt(FABRICATED_QQS_TILE, (finalQqsRow[0] * finalQsColumn[0]));
                Prefs.putInt(FABRICATED_QS_TILE, (finalQsColumn[0] * finalQsRow[0]));

                applyRowColumn();

                runOnUiThread(() -> {

                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        // Hide loading dialog
                        loadingDialog.hide();

                        // Reset button visibility
                        binding.qsRowColumnReset.setVisibility(View.VISIBLE);

                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_applied), Toast.LENGTH_SHORT).show();
                    }, 2000);
                });
            };
            Thread thread = new Thread(runnable);
            thread.start();
        });

        // Reset button
        binding.qsRowColumnReset.setVisibility(Prefs.getBoolean(QS_ROW_COLUMN_SWITCH) ? View.VISIBLE : View.GONE);

        binding.qsRowColumnReset.setOnClickListener(v -> {
            // Show loading dialog
            loadingDialog.show(getResources().getString(R.string.loading_dialog_wait));

            Runnable runnable = () -> {
                resetRowColumn();

                runOnUiThread(() -> {
                    Prefs.putBoolean(QS_ROW_COLUMN_SWITCH, false);

                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        // Hide loading dialog
                        loadingDialog.hide();

                        // Reset button visibility
                        binding.qsRowColumnReset.setVisibility(View.GONE);

                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_reset), Toast.LENGTH_SHORT).show();
                    }, 2000);
                });
            };
            Thread thread = new Thread(runnable);
            thread.start();
        });

        if (Prefs.getBoolean(ALERT_DIALOG_QSROWCOL, true)) {
            new MaterialAlertDialogBuilder(this, R.style.MaterialComponents_MaterialAlertDialog)
                    .setTitle(getResources().getString(R.string.hey_there))
                    .setMessage(getResources().getString(R.string.qs_row_column_warn_desc))
                    .setPositiveButton(getResources().getString(R.string.understood), (dialog, which) -> dialog.dismiss())
                    .setNegativeButton(getString(R.string.dont_show_again), (dialog, which) -> {
                        dialog.dismiss();
                        Prefs.putBoolean(ALERT_DIALOG_QSROWCOL, false);
                    })
                    .setCancelable(true)
                    .show();
        }
    }

    @Override
    public void onDestroy() {
        loadingDialog.hide();
        super.onDestroy();
    }
}