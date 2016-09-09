using UnityEngine;
using System.Collections;

public class MovingBallScript : MonoBehaviour {
	private RuntimePlatform platform = Application.platform;
	public GameObject particle;
	public float speed;
	private Vector3 target;
	static public Color ballColor;
	private AndroidJavaClass ajc;
	void Start(){
		target = transform.position;
		ajc = new AndroidJavaClass("com.GIP.VRAPP.UnityTalk"); 
		if (ballColor != null) {
			GameObject.Find ("MovingSphere").GetComponent<Renderer> ().material.color = 
				new Color ((int)ballColor.r, (int)ballColor.g, (int)ballColor.b);
		}
		if (platform == RuntimePlatform.Android || platform == RuntimePlatform.IPhonePlayer) {
			if (ajc != null) {
				speed = ajc.CallStatic<int> ("getSpeed");
			}
		} else if (platform == RuntimePlatform.WindowsEditor) {
			speed = 30;
		}
	}
	void Update(){
		float step = speed * Time.deltaTime;
		if(platform == RuntimePlatform.Android || platform == RuntimePlatform.IPhonePlayer){
			if (ajc != null) {
				int x = (int)(ajc.CallStatic<double> ("getX") * Screen.width);
				int y = (int)(ajc.CallStatic<double> ("getY") * Screen.height);
				float dis = (float)ajc.CallStatic<double> ("getDis");
				Vector3 pos = new Vector3 (x, y, dis);
				target = Camera.main.ScreenToWorldPoint (pos);
			}
		}else if(platform == RuntimePlatform.WindowsEditor || platform == RuntimePlatform.WindowsPlayer){
			if(Input.GetMouseButtonDown(0)) {
				Vector3 pos = new Vector3(Input.mousePosition.x, Input.mousePosition.y,10);
				target = Camera.main.ScreenToWorldPoint(pos);
			}
		}
		transform.position = Vector3.MoveTowards(transform.position,target,step);
	}
	public float realRGBtoUnityRGB(int color){
		return ((float)color / 255);
	}
}

