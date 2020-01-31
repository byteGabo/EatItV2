package com.example.eatitv2.Callback;

import com.example.eatitv2.Model.BestDealModel;
import com.example.eatitv2.Model.CategoryModel;

import java.util.List;

public interface ICategoryCallbackListener {
    void onCategoryLoadSuccess(List<CategoryModel> categoryModels);
    void onCategorylLoadFailed(String message);
}
