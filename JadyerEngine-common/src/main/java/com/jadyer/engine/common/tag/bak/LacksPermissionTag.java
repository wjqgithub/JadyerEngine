package com.jadyer.engine.common.tag.bak;

public class LacksPermissionTag extends PermissionTag {
	private static final long serialVersionUID = -6702087928102010950L;

	@Override
	protected boolean showTagBody(String permission){
		return !this.isPermitted(permission);
	}
}