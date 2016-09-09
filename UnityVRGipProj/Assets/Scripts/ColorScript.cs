using UnityEngine;
using System.Collections;
using UnityEngine.SceneManagement;

public class ColorScript : MonoBehaviour {
		private AndroidJavaClass ajc;
		void Start () {
			//OVRTouchpad.Create ();
			//OVRTouchpad.TouchHandler += OVRTouchpad_TouchHandler;
			//textObject = GameObject.Find("TryText").GetComponent<TextMesh>();

		ajc = new AndroidJavaClass("com.GIP.VRAPP.UnityTalk"); 

		}

		void OVRTouchpad_TouchHandler (object sender, System.EventArgs e)
		{
			//OVRTouchpad.TouchArgs touchArgs = (OVRTouchpad.TouchArgs)e;
			//if (touchArgs.TouchType == OVRTouchpad.TouchEvent.SingleTap) {
			//	ShouldMoveToSecondScene = true;
			//}
		}

		// Update is called once per frame
		void Update () {
		int r = 0;
		int g = 0;
		int b = 0;
		if (ajc != null) {
			r = ajc.CallStatic<int> ("getR");
			g = ajc.CallStatic<int> ("getG");
			b = ajc.CallStatic<int> ("getB");
		}
			
			//textObject.text = r.ToString() + " " + g.ToString() + " " + b.ToString() ;
			GameObject.Find("Sphere").GetComponent<Renderer>().material.color = 
				new Color(realRGBtoUnityRGB(r),realRGBtoUnityRGB(g),realRGBtoUnityRGB(b));
		if(Input.GetButton("Tap"))
		{
			MoveToSecondSceneSequence (r, g, b);
		}
		if (Input.touches.Length > 0 && Input.GetTouch(0).tapCount > 0) {
			MoveToSecondSceneSequence (r, g, b);
			}
		}

	public void MoveToSecondSceneSequence(int r,int g , int b) {
		MovingBallScript.ballColor = new Color (realRGBtoUnityRGB (r), realRGBtoUnityRGB (g), realRGBtoUnityRGB (b));
		ajc.CallStatic("moveToScene2");
		SceneManager.LoadScene ("MainScene");
		}

		public float realRGBtoUnityRGB(int color){
			return ((float)color / 255);
		}
	}
