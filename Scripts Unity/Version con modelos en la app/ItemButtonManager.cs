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
    
    void Start()
    {
        // Check if the values are not null before accessing
        if (!string.IsNullOrEmpty(itemName))
            transform.GetChild(0).GetComponent<Text>().text = itemName;

        if (itemImage != null)
        transform.GetChild(1).GetComponent<RawImage>().texture = itemImage.texture;
 
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
        if (item3DModel != null)
        interactionsManager.Item3DModel = Instantiate(item3DModel);
    }
    
}