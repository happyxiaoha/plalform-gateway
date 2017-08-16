package cn.dceast.platform.gateway.auth.data.entity;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FilterModel {

	public Map<String,String> step = new HashMap();

	public List<Map<String,String>> subStep = new ArrayList();

	public Map<String, String> getStep() {
		return step;
	}

	public void setStep(Map<String, String> step) {
		this.step = step;
	}

	public List<Map<String, String>> getSubStep() {
		return subStep;
	}

	public void setSubStep(List<Map<String, String>> subStep) {
		this.subStep = subStep;
	}


}
