package com.anthonysottile.kenken.ui;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.EventObject;
import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.anthonysottile.kenken.ui.CustomButton.CheckChangedEvent;

public class ValuesLayout extends LinearLayout {

	private static final LayoutParams buttonsLayoutParams =
		new LinearLayout.LayoutParams(
			30,
			ViewGroup.LayoutParams.MATCH_PARENT,
			0.5f
		);

	//#region Value Changed Event

	public class ValueEvent extends EventObject {

		private static final long serialVersionUID = 4677958792610955100L;

		private final int value;
		public int getValue() {
			return this.value;
		}

		public ValueEvent(Object sender, int value) {
			super(sender);

			this.value = value;
		}
	}

	public interface ValueChangedListener extends EventListener {
		void onValueChanged(ValueEvent event);
	}

	private final List<ValueChangedListener> valueChangedListeners =
		new ArrayList<ValueChangedListener>();
	public void AddValueChangedListener(ValueChangedListener listener) {
		this.valueChangedListeners.add(listener);
	}
	public void RemoveValueChangedListener(ValueChangedListener listener) {
		this.valueChangedListeners.remove(listener);
	}
	private void triggerValueChanged(int value) {
		ValueEvent event = new ValueEvent(this, value);

		for (ValueChangedListener listener : this.valueChangedListeners) {
			listener.onValueChanged(event);
		}
	}

	//#endregion

	private CustomButton[] valueButtons = null;
	private CustomButton selectedButton = null;

	private final CustomButton.CheckChangedListener checkChangedListener =
		new CustomButton.CheckChangedListener() {
			public void onCheckChanged(CheckChangedEvent event) {
				CustomButton valueButton = (CustomButton)event.getSource();

				if (valueButton.getChecked()) {
					// Button became checked
					if (ValuesLayout.this.selectedButton != null) {
						ValuesLayout.this.selectedButton.setCheckedNoTrigger(false);
						ValuesLayout.this.selectedButton = null;
					}

					ValuesLayout.this.selectedButton = valueButton;
					ValuesLayout.this.triggerValueChanged(valueButton.getValue());

				} else {

					// Button became unchecked
					ValuesLayout.this.selectedButton = null;
					ValuesLayout.this.triggerValueChanged(0);
				}
			}
		};

	/**
	 * Sets the values to disabled for all in the list of numbers.
	 *
	 * @param disabled The list of Values to disable.
	 */
	public void SetDisabled(List<Integer> disabled) {
		// First enable all the buttons
		for (CustomButton valueButton : this.valueButtons) {
			valueButton.setEnabled(true);
		}

		// Then disable the guys that we are supposed to
		int disabledSize = disabled.size();
		for (int i = 0; i < disabledSize; i += 1) {
			this.valueButtons[disabled.get(i) - 1].setEnabled(false);
		}
	}

	/**
	 * Disables all of the value buttons for this control.
	 */
	public void SetDisabled() {
		// Disable all of the buttons.
		for (CustomButton valueButton : this.valueButtons) {
			valueButton.setEnabled(false);
		}
	}

	/**
	 * Sets the value of this control.  Note, this does not bubble an event
	 *  as it should only be called when selecting a square.
	 *
	 * @param value The value to set.
	 */
	public void SetValue(int value) {

		// Do not trigger events as this should only be called from
		//  the setup/tear-down of a square being clicked.

		// Uncheck the current selected button if it is checked
		if (this.selectedButton != null) {
			this.selectedButton.setCheckedNoTrigger(false);
			this.selectedButton = null;
		}

		// If the value is not the "uncheck" value then set the check
		if (value != 0) {
			this.selectedButton = this.valueButtons[value - 1];
			this.selectedButton.setCheckedNoTrigger(true);
		}
	}

	/**
	 * Attempts to set the value on the control.  Note, this does bubble an event
	 *  as it should only be called from a ui element setting it (such as a key
	 *   press).  A value will not be set if the target button is disabled.
	 *   A value that is already set will be unset.
	 *
	 * @param value The value to attempt to set.
	 */
	public void TrySetValue(int value) {
		// Only set the value if it is enabled
		// Also when checking or un-checking the target button,
		//  trigger the check events.
		if (this.valueButtons[value - 1].getEnabled()) {
			if (this.valueButtons[value - 1].getChecked()) {
				// This button is currently selected
				// Uncheck it and set the currentSelectedButton to null
				this.selectedButton.setChecked(false);
				this.selectedButton = null;
			} else {

				// Uncheck the current selected button if it is checked
				if (this.selectedButton != null) {
					this.selectedButton.setCheckedNoTrigger(false);
					this.selectedButton = null;
				}

				this.selectedButton = this.valueButtons[value - 1];
				this.selectedButton.setChecked(true);
			}
		}
	}

	/**
	 * Clears the Value control removing all ui elements.
	 */
	public void Clear() {
		if (this.valueButtons != null) {
			this.removeAllViews();

			for (CustomButton valueButton : this.valueButtons) {
				valueButton.ClearCheckChangedListeners();
			}

			this.valueButtons = null;
		}
	}

	/**
	 * Setup method for when a new game is started.
	 *
	 * @param gameSize The size of the game.
	 */
	public void NewGame(int gameSize) {
		this.Clear();

		this.valueButtons = new CustomButton[gameSize];
		for (int i = 0; i < gameSize; i += 1) {
			this.valueButtons[i] = new CustomButton(this.getContext());
			this.valueButtons[i].setEnabled(true);
			this.valueButtons[i].setHasLeftCurve(false);
			this.valueButtons[i].setHasRightCurve(false);
			this.valueButtons[i].setIsCheckable(true);
			this.valueButtons[i].setCheckedNoTrigger(false);
			this.valueButtons[i].setValue(i + 1);
			this.valueButtons[i].setText(Integer.toString(i + 1, 10));
			this.valueButtons[i].AddCheckChangedListener(this.checkChangedListener);

			this.addView(this.valueButtons[i], ValuesLayout.buttonsLayoutParams);
		}

		// Set the first and last buttons to be curved (left and right).
		this.valueButtons[0].setHasLeftCurve(true);
		this.valueButtons[this.valueButtons.length - 1].setHasRightCurve(true);
	}

	public ValuesLayout(Context context, AttributeSet attrs) {
		super(context, attrs);

		this.setPadding(5, 15, 5, 15);
	}
}
