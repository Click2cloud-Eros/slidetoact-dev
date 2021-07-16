package com.ncorti.slidetoact.example;

import com.ncorti.slidetoact.example.slice.MainAbilitySlice;
import com.ncorti.slidetoact.example.slice.SampleAbilitySlice;
import ohos.aafwk.ability.Ability;
import ohos.aafwk.content.Intent;

public class MainAbility extends Ability {
    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setMainRoute(MainAbilitySlice.class.getName());
        addActionRoute(SampleAbilitySlice.LABEL, SampleAbilitySlice.class.getName());
    }
}
