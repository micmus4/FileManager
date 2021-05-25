# FileManager
File Manager project for Object Orientating Programming class at UAM (1st year, 2nd semester)

File Manager is a functional program with UI that enables user to perform several actions on files/directories.

User can:

 - inspect directory's content by clicking two times at directory (directories have '<DIR>' element after their name, other files don't)
 - go back to precedent directory by double clicking "[...]" element which is at the top of both lists, no matter which sorting option we choose.
 - create new directory in either left/or right path by pressing F7, which leads user to new window in which user can specify
   in which directory (the one from left side list or right side list) will the new directory be created. It won't be created if it contains illegal characters
   or a directory with the same name already exists.
 - delete selected file/directory when user presses F8. Before removing it from user's computer, the program will ask you to confirm the process.
 - sort elements of directories based on their name or date/time creation. Those options can be applied to both ascending and descending order.
 - copy file/directory to another directory by dragging them from one list and dropping them over to another list. If user copies directory consisting of other files/directories, those files/directories will also be copied to the newly copied directory.
 

  
Images: 
  
  ![0922a101a60502e2b116f7705573eb0a](https://user-images.githubusercontent.com/71464021/119261699-d8d37980-bbd8-11eb-980f-2134ac4a36b0.png)
  
  ![c271ef53a583aeb127b5e5753c7533fe](https://user-images.githubusercontent.com/71464021/119261705-e1c44b00-bbd8-11eb-9f75-a0a739eac069.png)

  ![ea42519e33d9765806ea03fa362b9348](https://user-images.githubusercontent.com/71464021/119261714-e8eb5900-bbd8-11eb-99dc-bcb77eb0d54c.png)
  
  
I used package from: https://commons.apache.org/proper/commons-io/ to perform some actions and also used one method from https://softwarecave.org/2018/03/24/delete-directory-with-contents-in-java/.


  
  
