using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using System;

public class GameManager : MonoBehaviour
{
    public event Action OnMainMenu;
    public event Action OnItemsMenu;
    public event Action OnARPosition;
    public static GameManager Instance;

    private void Awake()
    {
        if (Instance != null && Instance != this)
        {
            Destroy(gameObject);
        }
        else
        {
            Instance = this;
        }
    }

    // Corrección: Start con S mayúscula (Unity requiere esta convención)
    void Start()
    {
        MainMenu();
    }

    public void MainMenu()
    {
        OnMainMenu?.Invoke();
        Debug.Log("Main Menu Activated");
    }

    public void ItemsMenu()
    {
        OnItemsMenu?.Invoke();
        Debug.Log("Items Menu Activated");
    }

    public void ARPosition()
    {
        OnARPosition?.Invoke();
        // Corrección: Debug.Log() en lugar de Debug.log()
        Debug.Log("AR Position Activate");
    }

    // public void CloseAPP()
    //{
        // este debo ver si al final lo decido eliminar o no
        //Application.Quit();
    //}

    public void CloseAPP()
    {
    #if UNITY_ANDROID && !UNITY_EDITOR
        using (AndroidJavaClass unityPlayer = new AndroidJavaClass("com.unity3d.player.UnityPlayer"))
        {
            AndroidJavaObject currentActivity = unityPlayer.GetStatic<AndroidJavaObject>("currentActivity");
            currentActivity.Call("returnToAndroid");
        }
    #else
            Debug.Log("CloseAPP solo funciona en Android");
    #endif
        }
}



