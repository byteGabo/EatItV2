package com.example.eatitv2.Callback;

import com.example.eatitv2.Model.BestDealModel;
import com.example.eatitv2.Model.PopularCategoryModel;

import java.util.List;

public interface IBestDealCallbackListener {
    void onBestDealLoadSuccess(List<BestDealModel> bestDealModels);
    void onBestDealLoadFailed(String message);
}
