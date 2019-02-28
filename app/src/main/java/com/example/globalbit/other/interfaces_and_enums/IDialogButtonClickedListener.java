package com.example.globalbit.other.interfaces_and_enums;

public interface IDialogButtonClickedListener
{
	//NOTE:
	//this interface is in java so we can use SAM (single abstract method)
	//when implementing this interface with kotlin

	void onDialogButtonClicked();
}
