package com.ncorti.slidetoact.example.slice;

import com.ncorti.slidetoact.SlideToActView;
import com.ncorti.slidetoact.example.MainAbility;
import com.ncorti.slidetoact.example.ResourceTable;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;
import ohos.aafwk.content.Operation;
import ohos.agp.components.Button;
import ohos.agp.components.Component;

public class MainAbilitySlice extends AbilitySlice implements Component.ClickedListener{
    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setUIContent(ResourceTable.Layout_ability_main);

        findComponentById(ResourceTable.Id_button_area_margin).setClickedListener(this);
        findComponentById(ResourceTable.Id_button_icon_margin).setClickedListener(this);
        findComponentById(ResourceTable.Id_button_colors).setClickedListener(this);
        findComponentById(ResourceTable.Id_button_border_radius).setClickedListener(this);
    //    findComponentById(ResourceTable.Id_button_elevation).setClickedListener(this);
        findComponentById(ResourceTable.Id_button_text_size).setClickedListener(this);
        findComponentById(ResourceTable.Id_button_slider_dimension).setClickedListener(this);
        findComponentById(ResourceTable.Id_button_event_callbacks).setClickedListener(this);
        findComponentById(ResourceTable.Id_button_locked_slider).setClickedListener(this);
        findComponentById(ResourceTable.Id_button_custom_icon).setClickedListener(this);
        findComponentById(ResourceTable.Id_button_reversed_slider).setClickedListener(this);
        findComponentById(ResourceTable.Id_button_animation_duration).setClickedListener(this);
        findComponentById(ResourceTable.Id_button_bump_vibration).setClickedListener(this);

        SlideToActView slideToActView = (SlideToActView) findComponentById(ResourceTable.Id_welcome_slider);

        Button resetSlider = (Button) findComponentById(ResourceTable.Id_btn_reset_slider);
        resetSlider.setClickedListener(component -> slideToActView.resetSlider());
    }

    @Override
    public void onActive() {
        super.onActive();
    }

    @Override
    public void onForeground(Intent intent) {
        super.onForeground(intent);
    }

    @Override
    public void onClick(Component component) {
        Intent intent = new Intent();
        Operation operationLogin = new Intent.OperationBuilder().withBundleName(getBundleName())
                .withAbilityName(MainAbility.class.getName())
                .withAction(SampleAbilitySlice.LABEL)
                .build();
        intent.setOperation(operationLogin);
        intent.setParam(SampleAbilitySlice.EXTRA_PRESSED_BUTTON, component.getId());
        startAbility(intent);
    }
}
