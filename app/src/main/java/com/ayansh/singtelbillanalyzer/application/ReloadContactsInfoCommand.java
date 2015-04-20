package com.ayansh.singtelbillanalyzer.application;

import org.varunverma.CommandExecuter.Command;
import org.varunverma.CommandExecuter.Invoker;
import org.varunverma.CommandExecuter.ResultObject;

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
