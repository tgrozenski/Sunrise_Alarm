import { Text, View } from '@/components/Themed';
import React, { useState } from 'react';
import { Button, StyleSheet, FlatList, TouchableOpacity, SafeAreaView, StatusBar, } from 'react-native';
import Dialog from "react-native-dialog";

type ItemData = {
  id: string;
  title: string;
};

const DATA: ItemData[] = [];

type ItemProps = {
  item: ItemData;
  onPress: () => void;
  backgroundColor: string;
  textColor: string;
};

const Item = ({item, onPress, backgroundColor, textColor}: ItemProps) => (
  <TouchableOpacity onPress={onPress} style={[styles.item, {backgroundColor}]}>
    <Text style={[styles.title, {color: textColor}]}>{item.title}</Text>
  </TouchableOpacity>
);
var counter = 0;

const HomePage = () => {
  const [selectedId, setSelectedId] = useState<string>();
  const [visible, setVisible] = useState(false);
  const [text, setText] = useState('');
  const showDialog = () => {
    setVisible(true);
  };

  const handleCancel = () => {
    setVisible(false);
  };
  
  const handleSubmit = () => {
    setVisible(false);
    const newItem: ItemData = {
      id: '' + counter, 
      title: text,
    };
    counter++
    DATA.push(newItem);
    console.log(DATA);
  }
  function handleDelete(id: string) {
    
    const index = DATA.findIndex((item) => item.id === id);
    if (index !== -1) {
      // Remove the object from the array using splice()
      DATA.splice(index, 1);
      return true; // Indicate successful deletion
    } else {
      // Item not found
      return false; // Indicate unsuccessful deletion
    }

  }
  
  

  const renderItem = ({item}: {item: ItemData}) => {
    const backgroundColor = item.id === selectedId ? '#170f7a' : 'orange';
    const color = item.id === selectedId ? 'white' : 'black';

    return (
      
      <Item
        item={item}
        onPress={() => 
          //setSelectedId(item.id)
          // console.log('User pressed:' + item.id)
          handleDelete(item.id)        }
        backgroundColor={backgroundColor}
        textColor={color}
      />
      
    );
  };

  return (
    <SafeAreaView style={styles.container}>
      <FlatList
        data={DATA}
        renderItem={renderItem}
        keyExtractor={item => item.id}
        extraData={selectedId}
      />
      <View style={styles.container}>
      <Button title="Create a New Day" color='orange' onPress={showDialog} />
      <Dialog.Container visible={visible}>
        <Dialog.Title>Create A new Day</Dialog.Title>
        <Dialog.Input 
        placeholder="Enter the name of your day!"
        onChangeText={text => setText(text)}>
        </Dialog.Input>
        <Dialog.Button label="Cancel" onPress={handleCancel} />
        <Dialog.Button label="Submit" onPress={handleSubmit} />
      </Dialog.Container>
    </View>
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    marginTop: StatusBar.currentHeight || 0,
    backgroundColor:'dodgerblue',
    borderRadius: 10
  },
  item: {
    padding: 20,
    marginVertical: 8,
    marginHorizontal: 16,
    borderRadius:20
  },
  title: {
    fontSize: 32,
  },
}); 

export default HomePage