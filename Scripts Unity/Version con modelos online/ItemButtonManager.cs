using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;
using UnityEngine.Networking;

public class ItemButtonManager : MonoBehaviour
{
    private string itemName;
    private string itemDescription;
    private Sprite itemImage;
    private GameObject item3DModel;
    private ARInteractionManager interactionsManager;
    private string urlBundleModel;
    private RawImage imageBundle;
    // Add get accessors to allow reading the properties

    public string ItemName
    {
        get { return itemName; }
        set { itemName = value; }
    }
    public string ItemDescription
    {
        get { return itemDescription; }
        set { itemDescription = value; }
    }
    public Sprite ItemImage
    {
        get { return itemImage; }
        set { itemImage = value; }
    }
    public GameObject Item3DModel
    {
        get { return item3DModel; }
        set { item3DModel = value; }
    }
    public string URLBundleModel
    {
        get { return urlBundleModel; }
        set { urlBundleModel = value; }
    }
    public RawImage ImageBlunde
    {
        get { return imageBundle; }
        set { imageBundle = value; }
    }
    void Start()
    {
        // Check if the values are not null before accessing
        if (!string.IsNullOrEmpty(itemName))
            transform.GetChild(0).GetComponent<Text>().text = itemName;
        //if (itemImage != null)
        // transform.GetChild(1).GetComponent<RawImage>().texture = itemImage.texture;
        imageBundle = transform.GetChild(1).GetComponent<RawImage>();
        if (!string.IsNullOrEmpty(itemDescription))
            transform.GetChild(2).GetComponent<Text>().text = itemDescription;
        var button = GetComponent<Button>();
        // Use Instance (capital I) to match the GameManager script
        button.onClick.AddListener(GameManager.Instance.ARPosition);
        button.onClick.AddListener(Create3DModel);
        // Corregido: Reemplazado FindObjectOfType por FindFirstObjectByType
        interactionsManager = FindFirstObjectByType<ARInteractionManager>();
    }
    private void Create3DModel()
    {
        //if (item3DModel != null)
        //interactionsManager.Item3DModel = Instantiate(item3DModel);
        StartCoroutine(DownLoadAssetBundle(urlBundleModel));
    }
    IEnumerator DownLoadAssetBundle(string urlAssetBundle)
    {
        UnityWebRequest serverRequest = UnityWebRequestAssetBundle.GetAssetBundle(urlAssetBundle);
        yield return serverRequest.SendWebRequest();
        if (serverRequest.result == UnityWebRequest.Result.Success)
        {
            AssetBundle model3D = DownloadHandlerAssetBundle.GetContent(serverRequest);
            if (model3D != null)
            {
                interactionsManager.Item3DModel = Instantiate(model3D.LoadAsset(model3D.GetAllAssetNames()[0]) as GameObject);
            }
            else
            {
                Debug.Log("Not a valid Assets Bundle");
            }
        }
        else
        {
            Debug.Log("Error x'c");
        }
    }
}