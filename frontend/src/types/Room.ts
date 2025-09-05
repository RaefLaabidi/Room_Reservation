export interface Room {
  id: number;
  name: string;
  capacity: number;
  location: string;
}

export interface RoomCreateRequest {
  name: string;
  capacity: number;
  location: string;
}
