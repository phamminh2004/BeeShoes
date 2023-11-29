package fpoly.mds.beeshoes.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import fpoly.mds.beeshoes.R;
import fpoly.mds.beeshoes.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {
    FragmentHomeBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(getLayoutInflater());
        binding.tvShoeType.setOnClickListener(v -> {
            getActivity().setTitle("Loại giày");
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flContainer, new ShoeTypeFragment()).commit();
        });
        binding.tvShoes.setOnClickListener(v -> {
            getActivity().setTitle("Giày");
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flContainer, new ShoesFragment()).commit();
        });
        binding.tvEmployee.setOnClickListener(v -> {
            getActivity().setTitle("Nhân viên");
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flContainer, new EmployeeFragment()).commit();
        });
        binding.tvWork.setOnClickListener(v -> {
            getActivity().setTitle("Công việc");
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flContainer, new WorkFragment()).commit();
        });

        return binding.getRoot();
    }
}