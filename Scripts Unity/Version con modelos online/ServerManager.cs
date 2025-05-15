using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using System;
using UnityEngine.Networking;

public class ServerManager : MonoBehaviour
{
    [SerializeField] private string jsonURL;
    [SerializeField] private ItemButtonManager itemButtonManager;
    [SerializeField] private GameObject buttonsContainer;

    [Serializable]
    public struct Items
    {
        [Serializable]
        public struct Item
        {
            public string Name;
            public string Description;
            public string URLBundleModel;
            public string URLImageModel;
        }
        public Item[] items;
    }

    public Items newItemsCollection = new Items();

    void Start()
    {
        StartCoroutine(GetJsonData());
        GameManager.Instance.OnItemsMenu += CreateButtons;
    }

    private void CreateButtons()
    {
        foreach (var item in newItemsCollection.items)
        {
            ItemButtonManager itemButton;
            itemButton = Instantiate(itemButtonManager, buttonsContainer.transform);
            itemButton.name = item.Name;
            itemButton.ItemName = item.Name;
            itemButton.ItemDescription = item.Description;
            itemButton.URLBundleModel = item.URLBundleModel;
            StartCoroutine(GetBundleImage(item.URLImageModel, itemButton));
        }
        GameManager.Instance.OnItemsMenu -= CreateButtons;
    }

    IEnumerator GetJsonData()
    {
        UnityWebRequest serverRequest = UnityWebRequest.Get(jsonURL);
        yield return serverRequest.SendWebRequest();
        if (serverRequest.result == UnityWebRequest.Result.Success)
        {
            newItemsCollection = JsonUtility.FromJson<Items>(serverRequest.downloadHandler.text);
        }
        else
        {
            Debug.Log("Error :c");
        }
    }

    IEnumerator GetBundleImage(string urlImage, ItemButtonManager button)
    {
        UnityWebRequest serverRequest = UnityWebRequest.Get(urlImage);
        serverRequest.downloadHandler = new DownloadHandlerTexture();
        yield return serverRequest.SendWebRequest();
        if (serverRequest.result == UnityWebRequest.Result.Success)
        {
            button.ImageBlunde.texture = ((DownloadHandlerTexture)serverRequest.downloadHandler).texture;
        }
        else
        {
            Debug.Log("Error :c");
        }
    }
}
