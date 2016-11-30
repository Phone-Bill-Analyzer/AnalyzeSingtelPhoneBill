package com.ayansh.singtelbillanalyzer.application;

import com.ayansh.CommandExecuter.Command;
import com.ayansh.CommandExecuter.Invoker;
import com.ayansh.CommandExecuter.ResultObject;

public class ReloadContactsInfoCommand extends Command {

	public ReloadContactsInfoCommand(Invoker caller) {
		super(caller);
	}

	@Override
	protected void execute(ResultObject result) throws Exception {
		
		// Reload Contacts Info
		SBAApplication.getInstance().reloadContactsInfo();
		
	}

}
