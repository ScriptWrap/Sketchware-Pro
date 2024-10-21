package com.besome.sketch;

import android.Manifest;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.app.ActivityCompat;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.besome.sketch.fragments.ProjectsFragment;
import com.besome.sketch.fragments.projects_store.ProjectsStoreFragment;
import com.besome.sketch.lib.base.BasePermissionAppCompatActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.sketchware.remod.R;
import com.sketchware.remod.databinding.MainBinding;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.navigation.fragment.NavHostFragment;

import a.a.a.DB;
import a.a.a.GB;
import a.a.a.aB;
import a.a.a.bB;
import a.a.a.oB;
import a.a.a.sB;
import a.a.a.wq;
import a.a.a.xB;
import dev.chrisbanes.insetter.Insetter;
import dev.chrisbanes.insetter.Side;
import mod.SketchwareUtil;
import mod.agus.jcoderz.lib.FileUtil;
import mod.hey.studios.project.backup.BackupFactory;
import mod.hey.studios.project.backup.BackupRestoreManager;
import mod.hey.studios.util.Helper;
import mod.hilal.saif.activities.tools.ConfigActivity;
import mod.ilyasse.activities.about.AboutActivity;
import mod.ilyasse.utils.base.BottomSheetDialogView;
import mod.jbk.util.LogUtil;
import mod.tyron.backup.CallBackTask;
import mod.tyron.backup.SingleCopyAsyncTask;

public class MainActivity extends BasePermissionAppCompatActivity {
    private final OnBackPressedCallback closeDrawer = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            setEnabled(false);
            binding.drawerLayout.closeDrawers();
        }
    };

    private ActionBarDrawerToggle drawerToggle;
    private DB u;
    private Snackbar storageAccessDenied;
    private FragmentsAdapter fragmentsAdapter;
    private MainBinding binding;

    @Override
    // onRequestPermissionsResult but for Storage access only, and only when granted
    public void g(int i) {
        if (i == 9501) {
            allFilesAccessCheck();

            ProjectsFragment projectsFragment = fragmentsAdapter != null ? fragmentsAdapter.getProjectsFragment() : null;
            if (projectsFragment != null) {
                projectsFragment.refreshProjectsList();
            }
        }
    }

    @Override
    public void h(int i) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + getApplicationContext().getPackageName()));
        startActivityForResult(intent, i);
    }

    @Override
    public void l() {
    }

    @Override
    public void m() {
    }

    public void n() {
        ProjectsFragment projectsFragment = fragmentsAdapter != null ? fragmentsAdapter.getProjectsFragment() : null;
        if (projectsFragment != null) {
            projectsFragment.refreshProjectsList();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case 105:
                    sB.a(this, data.getBooleanExtra("onlyConfig", true));
                    break;

                case 111:
                    invalidateOptionsMenu();
                    break;

                case 113:
                    if (data != null && data.getBooleanExtra("not_show_popup_anymore", false)) {
                        u.a("U1I2", (Object) false);
                    }
                    break;

                case 212:
                    if (!(data.getStringExtra("save_as_new_id") == null ? "" : data.getStringExtra("save_as_new_id")).isEmpty() && isStoragePermissionGranted()) {
                        ProjectsFragment projectsFragment = fragmentsAdapter != null ? fragmentsAdapter.getProjectsFragment() : null;
                        if (projectsFragment != null) {
                            projectsFragment.refreshProjectsList();
                        }
                    }
                    break;
            }
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);

        tryLoadingCustomizedAppStrings();
        binding = MainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Insetter.builder()
                .padding(WindowInsetsCompat.Type.navigationBars(), Side.create(true, false, true, false))
                .applyToView(binding.layoutCoordinator);
        setSupportActionBar(binding.toolbar.toolbar);

        u = new DB(getApplicationContext(), "U1");
        int u1I0 = u.a("U1I0", -1);
        long u1I1 = u.e("U1I1");
        if (u1I1 <= 0) {
            u.a("U1I1", System.currentTimeMillis());
        }
        if (System.currentTimeMillis() - u1I1 > /* (a day) */ 1000 * 60 * 60 * 24) {
            u.a("U1I0", Integer.valueOf(u1I0 + 1));
        }

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(null);

        drawerToggle = new ActionBarDrawerToggle(this, binding.drawerLayout, R.string.app_name, R.string.app_name);
        binding.drawerLayout.addDrawerListener(drawerToggle);
        binding.drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                closeDrawer.setEnabled(true);
                getOnBackPressedDispatcher().addCallback(closeDrawer);
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
            }

            @Override
            public void onDrawerStateChanged(int newState) {
            }
        });
         
AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_store)
                .build();
   
        
        NavHostFragment navHostFragment =
    (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.frag);
    NavController navController = navHostFragment.getNavController();
    NavigationUI.setupWithNavController(binding.bottom, navController);


       binding.bottom.setOnItemSelectedListener(item -> {
                    Fragment fragment = null; 
                switch (item.getItemId()) { 
	                    case R.id.home: 
	                        Navigation.findNavController(binding.frag).navigate(R.id.navigation_home);
                            binding.createNewProject.show();
                        break; 
	                    case R.id.store: 
	                        Navigation.findNavController(binding.frag).navigate(R.id.navigation_store);
                            binding.createNewProject.hide();
                         break; 
	                }
	                return true;
	            
              });
	
	
	    

        boolean hasStorageAccess = isStoragePermissionGranted();
        if (!hasStorageAccess) {
            showNoticeNeedStorageAccess();
        }
        if (hasStorageAccess) {
            allFilesAccessCheck();
        }

        if (Intent.ACTION_VIEW.equals(getIntent().getAction())) {
            Uri data = getIntent().getData();
            if (data != null) {
                new SingleCopyAsyncTask(data, this, new CallBackTask() {
                    @Override
                    public void onCopyPreExecute() {
                    }

                    @Override
                    public void onCopyProgressUpdate(int progress) {
                    }

                    @Override
                    public void onCopyPostExecute(String path, boolean wasSuccessful, String reason) {
                        if (wasSuccessful) {
                            ProjectsFragment projectsFragment = fragmentsAdapter != null ? fragmentsAdapter.getProjectsFragment() : null;
                            BackupRestoreManager manager = new BackupRestoreManager(MainActivity.this, projectsFragment);

                            if (BackupFactory.zipContainsFile(path, "local_libs")) {
                                new MaterialAlertDialogBuilder(MainActivity.this)
                                        .setTitle("Warning")
                                        .setMessage(BackupRestoreManager.getRestoreIntegratedLocalLibrariesMessage(false, -1, -1, null))
                                        .setPositiveButton("Copy", (dialog, which) -> manager.doRestore(path, true))
                                        .setNegativeButton("Don't copy", (dialog, which) -> manager.doRestore(path, false))
                                        .setNeutralButton(R.string.common_word_cancel, null)
                                        .show();
                            } else {
                                manager.doRestore(path, true);
                            }

                            // Clear intent so it doesn't duplicate
                            getIntent().setData(null);
                        } else {
                            SketchwareUtil.toastError("Failed to copy backup file to temporary location: " + reason, Toast.LENGTH_LONG);
                        }
                    }
                }).execute(data);
            }
        } else if (!ConfigActivity.isSettingEnabled(ConfigActivity.SETTING_CRITICAL_UPDATE_REMINDER)) {

            BottomSheetDialogView bottomSheetDialog = getBottomSheetDialogView();

            bottomSheetDialog.getPositiveButton().setEnabled(false);

            CountDownTimer countDownTimer = new CountDownTimer(10000, 1000) {
                public void onTick(long millisUntilFinished) {
                    bottomSheetDialog.setPositiveButtonText(millisUntilFinished / 1000 + "");
                }

                public void onFinish() {
                    bottomSheetDialog.setPositiveButtonText("View changes");
                    bottomSheetDialog.getPositiveButton().setEnabled(true);
                }
            };
            countDownTimer.start();

            if (!MainActivity.this.isFinishing()) bottomSheetDialog.show();
        }
    }

    @NonNull
    private BottomSheetDialogView getBottomSheetDialogView() {
        BottomSheetDialogView bottomSheetDialog = new BottomSheetDialogView(this);
        bottomSheetDialog.setTitle("Major changes in v6.4.0");
        bottomSheetDialog.setDescription("""
                There have been major changes since v6.3.0 fix1, \
                and it's very important to know them all if you want your projects to still work.
                
                You can view all changes whenever you want at the About Sketchware Pro screen.""");

        bottomSheetDialog.setPositiveButton("View changes", (dialog, which) -> {
            ConfigActivity.setSetting(ConfigActivity.SETTING_CRITICAL_UPDATE_REMINDER, true);
            Intent launcher = new Intent(this, AboutActivity.class);
            launcher.putExtra("select", "changelog");
            startActivity(launcher);
        });
        bottomSheetDialog.setCancelable(false);
        return bottomSheetDialog;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        xB.b().a();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onResume() {
        super.onResume();
        /* Check if the device is running low on storage space */
        long freeMegabytes = GB.c();
        if (freeMegabytes < 100 && freeMegabytes > 0) {
            showNoticeNotEnoughFreeStorageSpace();
        }
        if (isStoragePermissionGranted() && storageAccessDenied != null && storageAccessDenied.isShown()) {
            storageAccessDenied.dismiss();
        }
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "MainActivity");
        bundle.putString(FirebaseAnalytics.Param.SCREEN_CLASS, "MainActivity");
        mAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
    }

    private void allFilesAccessCheck() {
        if (Build.VERSION.SDK_INT > 29) {
            File optOutFile = new File(getFilesDir(), ".skip_all_files_access_notice");
            boolean granted = Environment.isExternalStorageManager();

            if (!optOutFile.exists() && !granted) {
                aB dialog = new aB(this);
                dialog.a(R.drawable.ic_expire_48dp);
                dialog.b("Android 11 storage access");
                dialog.a("Starting with Android 11, Sketchware Pro needs a new permission to avoid " +
                        "taking ages to build projects. Don't worry, we can't do more to storage than " +
                        "with current granted permissions.");
                dialog.b(Helper.getResString(R.string.common_word_settings), v -> {
                    FileUtil.requestAllFilesAccessPermission(this);
                    dialog.dismiss();
                });
                dialog.a("Skip", Helper.getDialogDismissListener(dialog));
                dialog.configureDefaultButton("Don't show anymore", v -> {
                    try {
                        if (!optOutFile.createNewFile())
                            throw new IOException("Failed to create file " + optOutFile);
                    } catch (IOException e) {
                        Log.e("MainActivity", "Error while trying to create " +
                                "\"Don't show Android 11 hint\" dialog file: " + e.getMessage(), e);
                    }
                    dialog.dismiss();
                });
                dialog.show();
            }
        }
    }

    private void showNoticeNeedStorageAccess() {
        aB dialog = new aB(this);
        dialog.b(Helper.getResString(R.string.common_message_permission_title_storage));
        dialog.a(R.drawable.color_about_96);
        dialog.a(Helper.getResString(R.string.common_message_permission_need_load_project));
        dialog.b(Helper.getResString(R.string.common_word_ok), v -> {
            dialog.dismiss();
            ActivityCompat.requestPermissions(this, new String[]{
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE},
                    9501);
        });
        dialog.show();
    }

    private void showNoticeNotEnoughFreeStorageSpace() {
        aB dialog = new aB(this);
        dialog.b(Helper.getResString(R.string.common_message_insufficient_storage_space_title));
        dialog.a(R.drawable.high_priority_96_red);
        dialog.a(Helper.getResString(R.string.common_message_insufficient_storage_space));
        dialog.b(Helper.getResString(R.string.common_word_ok), Helper.getDialogDismissListener(dialog));
        dialog.show();
    }

    public void s() {
        if (storageAccessDenied == null || !storageAccessDenied.isShown()) {
            storageAccessDenied = Snackbar.make(binding.layoutCoordinator, Helper.getResString(R.string.common_message_permission_denied), Snackbar.LENGTH_INDEFINITE);
            storageAccessDenied.setAction(Helper.getResString(R.string.common_word_settings), v -> {
                storageAccessDenied.dismiss();
                ActivityCompat.requestPermissions(this, new String[]{
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE},
                        9501);
            });
            storageAccessDenied.setActionTextColor(Color.YELLOW);
            storageAccessDenied.show();
        }
    }

    //This is annoying Please remove/togglize it
    private void tryLoadingCustomizedAppStrings() {
        // Refresh extracted provided strings file if necessary
        oB oB = new oB();
        try {
            File extractedStringsProvidedXml = new File(wq.m());
            if (oB.a(getApplicationContext(), "localization/strings.xml") !=
                    (extractedStringsProvidedXml.exists() ? extractedStringsProvidedXml.length() : 0)) {
                oB.a(extractedStringsProvidedXml);
                oB.a(getApplicationContext(), "localization/strings.xml", wq.m());
            }
        } catch (Exception e) {
            String message = "Couldn't extract default strings to storage";
            SketchwareUtil.toastError(message + ": " + e.getMessage());
            LogUtil.e("MainActivity", message, e);
        }

        // Actual loading part
        if (xB.b().b(getApplicationContext())) {
            bB.a(getApplicationContext(),
                    Helper.getResString(R.string.message_strings_xml_loaded),
                    0, 80, 0, 128).show();
        }
    }

    // ----------------- Inner Classes ----------------- //

   public static class FragmentsAdapter extends FragmentStateAdapter {

        private final ProjectsFragment projectsFragment;
        private final ProjectsStoreFragment projectsStoreFragment;

        public FragmentsAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
            projectsFragment = new ProjectsFragment();
            projectsStoreFragment = new ProjectsStoreFragment();
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            if (position == 1) {
                return projectsStoreFragment;
            } else {
                return projectsFragment;
            }
        }

        @Override
        public int getItemCount() {
            return 2;
        }

        public ProjectsFragment getProjectsFragment() {
            return projectsFragment;
        }

        public ProjectsStoreFragment getProjectsStoreFragment() {
            return projectsStoreFragment;
        }
   }
    //--------------New Class-----------------//
    

   
}
