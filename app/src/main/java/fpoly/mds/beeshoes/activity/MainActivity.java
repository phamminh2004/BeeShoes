package fpoly.mds.beeshoes.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import fpoly.mds.beeshoes.R;
import fpoly.mds.beeshoes.fragment.AddAccountFragment;
import fpoly.mds.beeshoes.fragment.ChangePasswordFragment;
import fpoly.mds.beeshoes.fragment.CustomerFragment;
import fpoly.mds.beeshoes.fragment.EmployeeFragment;
import fpoly.mds.beeshoes.fragment.HomeCustomerFragment;
import fpoly.mds.beeshoes.fragment.HomeFragment;
import fpoly.mds.beeshoes.fragment.BillFragment;
import fpoly.mds.beeshoes.fragment.RevenueStatisticsFragment;
import fpoly.mds.beeshoes.fragment.ShoeTypeFragment;
import fpoly.mds.beeshoes.fragment.ShoesFragment;
import fpoly.mds.beeshoes.fragment.WorkFragment;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    DrawerLayout drawer;
    Toolbar toolbar;
    View mHeaderView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        drawer = findViewById(R.id.drawer_layout);
        toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> {
            onBackPressed();
        });
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setHomeButtonEnabled(true);
        NavigationView nv = findViewById(R.id.nvView);
        nv.setNavigationItemSelectedListener(this);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.open, R.string.close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        mHeaderView = nv.getHeaderView(0);
        Intent intent = getIntent();
        String role = intent.getStringExtra("role");

        if ("manager".equals(role)) {
            nv.getMenu().findItem(R.id.addAccount).setVisible(true);
            nv.getMenu().findItem(R.id.order).setVisible(false);
            getSupportFragmentManager().beginTransaction().replace(R.id.flContainer, new HomeFragment()).commit();
        } else if (role.equals("employee")) {
            nv.getMenu().findItem(R.id.order).setVisible(false);
            getSupportFragmentManager().beginTransaction().replace(R.id.flContainer, new HomeFragment()).commit();
        } else {
            nv.getMenu().findItem(R.id.homeManage).setVisible(false);
            nv.getMenu().findItem(R.id.manage).setVisible(false);
            nv.getMenu().findItem(R.id.statistic).setVisible(false);
            nv.getMenu().findItem(R.id.homeCustomer).setVisible(true);
            nv.getMenu().findItem(R.id.bill).setVisible(true);
            getSupportFragmentManager().beginTransaction().replace(R.id.flContainer, new HomeCustomerFragment()).commit();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.logout) {
            startActivity(new Intent(MainActivity.this, Login.class));
            finish();
        } else if (item.getItemId() == R.id.changePassword) {
            setTitle("Đổi mật khẩu");
            getSupportFragmentManager().beginTransaction().replace(R.id.flContainer, new ChangePasswordFragment()).addToBackStack(null).commit();
        } else if (item.getItemId() == R.id.addAccount) {
            setTitle("Thêm tài khoản");
            getSupportFragmentManager().beginTransaction().replace(R.id.flContainer, new AddAccountFragment()).addToBackStack(null).commit();
        } else if (item.getItemId() == R.id.homeManage) {
            setTitle("Trang chủ");
            getSupportFragmentManager().beginTransaction().replace(R.id.flContainer, new HomeFragment()).addToBackStack(null).commit();
        } else if (item.getItemId() == R.id.shoeType) {
            setTitle("Loại giày");
            getSupportFragmentManager().beginTransaction().replace(R.id.flContainer, new ShoeTypeFragment()).addToBackStack(null).commit();
        } else if (item.getItemId() == R.id.shoes) {
            setTitle("Giày");
            getSupportFragmentManager().beginTransaction().replace(R.id.flContainer, new ShoesFragment()).addToBackStack(null).commit();
        } else if (item.getItemId() == R.id.employee) {
            setTitle("Nhân viên");
            getSupportFragmentManager().beginTransaction().replace(R.id.flContainer, new EmployeeFragment()).addToBackStack(null).commit();
        } else if (item.getItemId() == R.id.work) {
            setTitle("Công việc");
            getSupportFragmentManager().beginTransaction().replace(R.id.flContainer, new WorkFragment()).addToBackStack(null).commit();
        } else if (item.getItemId() == R.id.customer) {
            setTitle("Khách hàng");
            getSupportFragmentManager().beginTransaction().replace(R.id.flContainer, new CustomerFragment()).addToBackStack(null).commit();
        } else if (item.getItemId() == R.id.homeCustomer) {
            setTitle("Trang chủ");
            getSupportFragmentManager().beginTransaction().replace(R.id.flContainer, new HomeCustomerFragment()).addToBackStack(null).commit();
        } else if (item.getItemId() == R.id.bill) {
            setTitle("Hóa đơn");
            getSupportFragmentManager().beginTransaction().replace(R.id.flContainer, new BillFragment()).addToBackStack(null).commit();
        }else if (item.getItemId() == R.id.order) {
            setTitle("Đơn hàng");
            getSupportFragmentManager().beginTransaction().replace(R.id.flContainer, new BillFragment()).addToBackStack(null).commit();
        }else if (item.getItemId() == R.id.revenue) {
            setTitle("Thống kê doanh thu");
            getSupportFragmentManager().beginTransaction().replace(R.id.flContainer, new RevenueStatisticsFragment()).addToBackStack(null).commit();
        }
        drawer.close();
        return false;
    }
}