import React, { useState, useEffect } from 'react';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/card';
import { Tabs, TabsList, TabsTrigger, TabsContent } from '@/components/ui/tabs';
import { Badge } from '@/components/ui/badge';
import { PieChart, Pie, Cell, BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';

const AllocationDashboard = () => {
  const [data, setData] = useState(null);
  
  useEffect(() => {
    const loadData = async () => {
      try {
        const fileContent = await window.fs.readFile('allocation_results.json', { encoding: 'utf8' });
        setData(JSON.parse(fileContent));
      } catch (error) {
        console.error('Error loading allocation results:', error);
      }
    };
    loadData();
  }, []);

  if (!data) return <div className="p-4">Loading allocation data...</div>;

  const { allocation, statistics } = data;
  const { rooms, unallocatedCourses } = allocation;

  // Calculate statistics
  const totalRooms = rooms.length;
  const allocatedRooms = rooms.filter(room => room.course).length;
  const occupancyRate = (allocatedRooms / totalRooms * 100).toFixed(1);

  // Group rooms by type
  const roomsByType = rooms.reduce((acc, room) => {
    if (!acc[room.type]) acc[room.type] = [];
    acc[room.type].push(room);
    return acc;
  }, {});

  // Prepare data for pie chart
  const pieData = [
    { name: 'Allocated', value: allocatedRooms },
    { name: 'Empty', value: totalRooms - allocatedRooms }
  ];
  const COLORS = ['#4ade80', '#f87171'];

  // Prepare data for bar chart
  const barData = Object.entries(roomsByType).map(([type, rooms]) => ({
    type: type.replace(/_/g, ' '),
    total: rooms.length,
    allocated: rooms.filter(r => r.course).length
  }));

  return (
    <div className="w-full max-w-7xl mx-auto p-4 space-y-4">
      {/* Summary Cards */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium">Total Rooms</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{totalRooms}</div>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium">Allocated Rooms</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{allocatedRooms}</div>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium">Occupancy Rate</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{occupancyRate}%</div>
          </CardContent>
        </Card>
      </div>

      <Tabs defaultValue="overview" className="space-y-4">
        <TabsList>
          <TabsTrigger value="overview">Overview</TabsTrigger>
          <TabsTrigger value="details">Room Details</TabsTrigger>
          <TabsTrigger value="unallocated">Unallocated Courses</TabsTrigger>
        </TabsList>

        <TabsContent value="overview" className="space-y-4">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <Card>
              <CardHeader>
                <CardTitle>Room Allocation Status</CardTitle>
              </CardHeader>
              <CardContent className="h-80">
                <ResponsiveContainer width="100%" height="100%">
                  <PieChart>
                    <Pie
                      data={pieData}
                      cx="50%"
                      cy="50%"
                      innerRadius={60}
                      outerRadius={80}
                      fill="#8884d8"
                      paddingAngle={5}
                      dataKey="value"
                      label={({ name, value }) => `${name}: ${value}`}
                    >
                      {pieData.map((entry, index) => (
                        <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                      ))}
                    </Pie>
                    <Tooltip />
                  </PieChart>
                </ResponsiveContainer>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>Allocation by Room Type</CardTitle>
              </CardHeader>
              <CardContent className="h-80">
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart data={barData}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="type" angle={-45} textAnchor="end" height={100} />
                    <YAxis />
                    <Tooltip />
                    <Legend />
                    <Bar dataKey="total" name="Total Rooms" fill="#93c5fd" />
                    <Bar dataKey="allocated" name="Allocated" fill="#4ade80" />
                  </BarChart>
                </ResponsiveContainer>
              </CardContent>
            </Card>
          </div>
        </TabsContent>

        <TabsContent value="details">
          <Card>
            <CardContent className="p-6">
              <div className="space-y-6">
                {Object.entries(roomsByType).map(([type, rooms]) => (
                  <div key={type} className="space-y-2">
                    <h3 className="font-semibold text-lg">{type.replace(/_/g, ' ')}</h3>
                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                      {rooms.map(room => (
                        <div key={room.name} className="p-4 border rounded-lg bg-card">
                          <div className="flex justify-between items-start">
                            <div>
                              <h4 className="font-medium">{room.name}</h4>
                              <p className="text-sm text-muted-foreground">
                                Capacity: {room.capacity}
                              </p>
                            </div>
                            <Badge variant={room.course ? "success" : "secondary"}>
                              {room.course ? 'Allocated' : 'Empty'}
                            </Badge>
                          </div>
                          {room.course && (
                            <div className="mt-2 text-sm">
                              <p>Course: {room.course.name}</p>
                              <p>Size: {room.course.size}</p>
                            </div>
                          )}
                        </div>
                      ))}
                    </div>
                  </div>
                ))}
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="unallocated">
          <Card>
            <CardHeader>
              <CardTitle>Unallocated Courses ({unallocatedCourses.length})</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                {unallocatedCourses.map(course => (
                  <div key={course.name} className="p-4 border rounded-lg">
                    <h4 className="font-medium">{course.name}</h4>
                    <p className="text-sm text-muted-foreground">Size: {course.size}</p>
                  </div>
                ))}
              </div>
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  );
};

export default AllocationDashboard;